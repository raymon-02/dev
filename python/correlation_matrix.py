import argparse
from collections import defaultdict
from os import path
from os import walk

import numpy as np
from matplotlib import cm as cm
from matplotlib import pyplot as plt

default_path_to_data = "./results"
default_path_to_regions = "./countries/regions"


class FileCountry:
    def __init__(self, file, countries):
        self.file = file
        self.countries = countries

    def __str__(self):
        return "{{file={},counties={}}}".format(self.file, self.countries)

    def __repr__(self):
        return "{{file={},counties={}}}".format(self.file, self.countries)


def get_path_to_result(path_to_data):
    for root, _, files in walk(path_to_data):
        return path.join(root, files[-1])


def parse_file(line):
    return line.split(":")[1].strip()


def parse_countries(line):
    countries = line.split(":")[1].strip()
    if not countries:
        return []
    return countries.split(",")


def reformat_lines(lines):
    result = []
    for line in lines:
        if line.startswith("decode") or line.startswith("bad"):
            result.pop()
            result.pop()
            continue
        result.append(line)
    return result


def read_result(path_to_result):
    result = []

    with open(path_to_result, "r") as handler:
        lines = reformat_lines(handler.readlines())
        for i in range(0, len(lines), 4):
            if lines[i].startswith("##"):
                break
            file = parse_file(lines[i])
            countries = parse_countries(lines[i + 2])
            if not countries:
                continue
            result.append(FileCountry(file, countries))

    return result


def build_country_files(file_countries):
    result = defaultdict(set)

    for file_country in file_countries:
        for country in file_country.countries:
            result[country].add(file_country.file)

    return result


def read_countries_regions(path_to_regions):
    with open(path_to_regions) as handler:
        lines = handler.readlines()

    return [line.strip() for line in lines]


def validate(country_files, countries_regions):
    countries_regions = set(country for country in countries_regions)
    for country in country_files.keys():
        if country not in countries_regions:
            print(country)


def build_dimensional_matrix(file_countries, path_to_regions):
    dimensional_matrix = []
    country_vector = []
    country_files = build_country_files(file_countries)
    countries_regions = read_countries_regions(path_to_regions)

    validate(country_files, countries_regions)

    for country in countries_regions:
        if country not in country_files:
            continue
        files = country_files.get(country)
        country_dimensional = [1 if file_country.file in files else 0 for file_country in file_countries]
        dimensional_matrix.append(country_dimensional)
        country_vector.append(country)

    return dimensional_matrix, country_vector


def draw_correlation_matrix(correlation_matrix, country_vector):
    length = len(country_vector)
    fig = plt.figure()
    ax = fig.add_subplot(111)
    cmap = cm.get_cmap('jet', 30)
    cax = ax.imshow(correlation_matrix, interpolation="nearest", cmap=cmap)
    plt.title("Country correlation")
    ax.set_xticks(np.arange(length))
    ax.set_yticks(np.arange(length))
    ax.set_xticklabels(country_vector, fontsize=6, rotation=90)
    ax.set_yticklabels(country_vector, fontsize=6)
    fig.colorbar(cax, ticks=[x / 100 for x in range(-100, 100, 5)])
    plt.show()


def main():
    parser = argparse.ArgumentParser(description='Command line arguments')
    parser.add_argument('-d', '--data', default=default_path_to_data,
                        help='path to dir with data')
    parser.add_argument('-r', '--regions', default=default_path_to_regions,
                        help='path to dir with country list by regions')
    args = parser.parse_args()
    path_to_data = args.data
    path_to_regions = args.regions

    path_to_result = get_path_to_result(path_to_data)
    file_countries = read_result(path_to_result)
    dimensional_matrix, country_vector = build_dimensional_matrix(file_countries, path_to_regions)
    correlation_matrix = np.corrcoef(dimensional_matrix)

    draw_correlation_matrix(correlation_matrix, country_vector)


if __name__ == '__main__':
    main()
