package com.w6.data;

import java.util.List;

public class Table {
    final List<String> who, weapon, what, whom, where, when, country;

    public List<String> getWhere() {
        return where;
    }

    public Table(
            List<String> who,
            List<String> weapon,
            List<String> what,
            List<String> whom,
            List<String> where,
            List<String> when,
            List<String> country
    ) {
        this.who = who;
        this.weapon = weapon;
        this.what = what;
        this.whom = whom;
        this.where = where;
        this.when = when;
        this.country = country;
    }
}
