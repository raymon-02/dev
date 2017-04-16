import argparse
import json
import time
from os import path
from os import walk

import requests
from bs4 import BeautifulSoup

default_path_to_data = "./data"
default_path_to_result = "./result"


class ProceedArticle:
    def __init__(self, exact, countries):
        self.exact = exact
        self.countries = countries


class BadResponse(Exception):
    pass


def extract_countries(script):
    json_country = ""
    for line in script.split("\n"):
        if "\\\"country\\\"" in line:
            json_country = line.strip()[11:-1]

    json_response = json.loads(json_country)["response"]
    return json.loads(json_response)["table"]["country"]


def parse_response(response):
    soup = BeautifulSoup(response.content, "html.parser")
    scripts = soup.find_all("script")
    if not scripts:
        raise BadResponse()

    script = scripts[-1].string
    return extract_countries(script)


def send_request(text):
    url = "http://localhost:8084/post"
    login_data = dict(login="user", password="user")
    params = dict(source="empty", title="empty", text=text)
    response = requests.post(url, data=login_data, params=params)

    return response


def extract_answer_chunks(text):
    chunks = text.split()

    location_index = -1
    for i in range(0, len(chunks)):
        if "Location" in chunks[i]:
            location_index = i
            break

    if location_index == -1:
        return []

    return chunks[location_index:location_index + 3]


def process_chunk(chunk):
    letters = [chunk[0]]
    for letter in chunk[1:]:
        if letter.isupper():
            break
        letters.append(letter)

    return "".join(letters)


def process_answer_chunks(answer_chunks):
    if len(answer_chunks) == 0:
        return "no"

    first_chunk = answer_chunks[0]
    first_chunk_split = first_chunk.split(':')

    if len(first_chunk_split) > 2:
        return process_chunk(first_chunk_split[1])

    answer = [answer_chunks[-1]]
    for chunk in answer_chunks:
        chunk_split = chunk.split(':')
        answer.append(process_chunk(chunk_split[0]))
        if len(chunk_split) > 1:
            break

    return " ".join(answer)


def extract_exact_answer(text):
    answer_chunks = extract_answer_chunks(text)
    return process_answer_chunks(answer_chunks)


def extract_countries_from_w6(text):
    response = send_request(text)
    return parse_response(response)


def handle_article(handler):
    lines = handler.readlines()
    text = "\n".join(lines)

    # TODO: work partly
    exact_answer = extract_exact_answer(text)
    countries = extract_countries_from_w6(text)

    return ProceedArticle(exact_answer, countries)


def write_result(handler, result, file):
    handler.write("File: {}\n".format(file))
    handler.write("exact answer: {}\n".format(result.exact))
    handler.write("countries:    {}\n\n".format(",".join(result.countries)))


def write_decode_error(handler, file):
    handler.write("File: {}\n".format(file))
    handler.write("decode error\n\n")


def write_bad_response(handler, file):
    handler.write("File: {}\n".format(file))
    handler.write("bad response\n\n")


def main():
    parser = argparse.ArgumentParser(description='Command line arguments')
    parser.add_argument('-d', '--data', default=default_path_to_data,
                        help='path to folder with articles')
    parser.add_argument('-r', '--result', default=default_path_to_result,
                        help='path to file with results')

    args = parser.parse_args()
    path_to_data = args.data
    path_to_result = args.result

    all_time = 0
    articles_count = 0
    decode_error_count = 0
    bad_response_count = 0
    print("App has been started\n")

    with open(path_to_result, "w") as handler_writer:
        for root, _, files in walk(path_to_data):
            for file in files:
                with open(path.join(root, file)) as handler_reader:
                    try:
                        print("Start proceeded: {}".format(file))

                        start = time.time()
                        result = handle_article(handler_reader)
                        write_result(handler_writer, result, file)
                        end = time.time()

                        article_time = (end - start) * 1000
                        all_time += article_time
                        articles_count += 1

                        print("Proceeded:       {} in {:0.2f} ms\n".format(file, article_time))
                    except UnicodeDecodeError:
                        decode_error_count += 1
                        print("Decode error:    {}\n".format(file))
                        write_decode_error(handler_writer, file)
                    except BadResponse:
                        bad_response_count += 1
                        print("Bad response:    {}\n".format(file))
                        write_bad_response(handler_writer, file)

    print("App has been finished")
    print("{} articles have been proceeded in {:0.2f} ms".format(articles_count, all_time))
    print("{} articles have decode error".format(decode_error_count))
    print("{} articles have bad response".format(bad_response_count))


if __name__ == "__main__":
    main()
