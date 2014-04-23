#!/usr/bin/env python
# -*- encoding: UTF-8 -*-


from __future__ import print_function

import random


"""
Node labels:
    :Person
    :Food

Relationship types:
    KNOWS
    LIKES
    DISLIKES

Allowed patterns:
    (:Person)-[:KNOWS]->(:Person)
    (:Person)-[:LIKES]->(:Person)
    (:Person)-[:DISLIKES]->(:Person)
    (:Person)-[:LIKES]->(:Food)
    (:Person)-[:DISLIKES]->(:Food)
"""


BOYS_NAMES = ['William', 'John', 'George', 'Thomas', 'James', 'Arthur',
              'Frederick', 'Charles', 'Albert', 'Robert', 'Joseph', 'Alfred',
              'Henry', 'Ernest', 'Harry', 'Harold', 'Edward', 'Walter',
              'Frank', 'Herbert', 'Richard', 'Reginald', 'Percy', 'Leonard',
              'Samuel', 'David', 'Sidney', 'Francis', 'Stanley', 'Fred',
              'Cecil', 'Horace', 'Cyril', 'Wilfred', 'Sydney', 'Leslie',
              'Norman', 'Edwin', 'Victor', 'Benjamin', 'Tom', 'Hector',
              'Jack', 'Alexander', 'Edgar', 'Bertie', 'Eric', 'Philip',
              'Clifford', 'Redvers', 'Baden', 'Bernard', 'Daniel', 'Donald',
              'Ralph', 'Archibald', 'Stephen', 'Willie', 'Peter',
              'Christopher', 'Hugh', 'Lewis', 'Douglas', 'Gilbert', 'Ronald',
              'Isaac', 'Hubert', 'Maurice', 'Clarence', 'Lawrence', 'Michael',
              'Edmund', 'Patrick', 'Percival', 'Andrew', 'Matthew', 'Evan',
              'Wilfrid', 'Bertram', 'Louis', 'Arnold', 'Kenneth', 'Gordon',
              'Ivor', 'Gerald', 'Abraham', 'Geoffrey', 'Owen', 'Raymond',
              'Oliver', 'Claude', 'Alan', 'Mark', 'Jesse', 'Reuben', 'Roland',
              'Lionel', 'Alec', 'Charlie', 'Howard']
GIRLS_NAMES = ['Mary', 'Florence', 'Annie', 'Edith', 'Alice', 'Elizabeth',
               'Elsie', 'Dorothy', 'Ethel', 'Doris', 'Margaret', 'Gladys',
               'Sarah', 'Lilian', 'Ellen', 'Hilda', 'Lily', 'Winifred',
               'Violet', 'Ada', 'Emily', 'Beatrice', 'Nellie', 'May', 'Mabel',
               'Ivy', 'Rose', 'Gertrude', 'Jane', 'Catherine', 'Kathleen',
               'Frances', 'Agnes', 'Olive', 'Jessie', 'Emma', 'Eva', 'Minnie',
               'Maud', 'Louisa', 'Amy', 'Grace', 'Clara', 'Martha', 'Daisy',
               'Evelyn', 'Hannah', 'Lucy', 'Kate', 'Eliza', 'Bertha', 'Ann',
               'Eleanor', 'Harriet', 'Phyllis', 'Constance', 'Dora', 'Ida',
               'Esther', 'Isabella', 'Nora', 'Marjorie', 'Laura', 'Charlotte',
               'Irene', 'Ruth', 'Bessie', 'Caroline', 'Fanny', 'Muriel',
               'Maggie', 'Edna', 'Norah', 'Amelia', 'Helen', 'Mildred',
               'Vera', 'Gwendoline', 'Eveline', 'Lizzie', 'Marion', 'Rachel',
               'Rosina', 'Florrie', 'Maria', 'Lydia', 'Ruby', 'Victoria',
               'Miriam', 'Blanche', 'Rosa', 'Rebecca', 'Julia', 'Ella',
               'Henrietta', 'Isabel', 'Matilda', 'Janet', 'Phoebe', 'Susan']
GIVEN_NAMES = BOYS_NAMES + GIRLS_NAMES
FAMILY_NAMES = ['Smith', 'Jones', 'Taylor', 'Williams', 'Brown', 'Davies',
                'Evans', 'Wilson', 'Thomas', 'Roberts', 'Johnson', 'Lewis',
                'Walker', 'Robinson', 'Wood', 'Thompson', 'White', 'Watson',
                'Jackson', 'Wright', 'Green', 'Harris', 'Cooper', 'King',
                'Lee', 'Martin', 'Clarke', 'James', 'Morgan', 'Hughes',
                'Edwards', 'Hill', 'Moore', 'Clark', 'Harrison', 'Scott',
                'Young', 'Morris', 'Hall', 'Ward', 'Turner', 'Carter',
                'Phillips', 'Mitchell', 'Patel', 'Adams', 'Campbell',
                'Anderson', 'Allen', 'Cook', 'Bailey', 'Parker', 'Miller',
                'Davis', 'Murphy', 'Price', 'Bell', 'Baker', 'Griffiths',
                'Kelly', 'Simpson', 'Marshall', 'Collins', 'Bennett', 'Cox',
                'Richardson', 'Fox', 'Gray', 'Rose', 'Chapman', 'Hunt',
                'Robertson', 'Shaw', 'Reynolds', 'Lloyd', 'Ellis', 'Richards',
                'Russell', 'Wilkinson', 'Khan', 'Graham', 'Stewart', 'Reid',
                'Murray', 'Powell', 'Palmer', 'Holmes', 'Rogers', 'Stevens',
                'Walsh', 'Hunter', 'Thomson', 'Matthews', 'Ross', 'Owen',
                'Mason', 'Knight', 'Kennedy', 'Butler', 'Saunders']

FOODS = ["apples", "bananas", "bread", "coffee", "cheese", "chicken", "yogurt",
         "soup", "eggs", "milk", "tea", "rice", "salt", "pepper", "carrots",
         "beef", "lamb", "tomatoes", "potatoes", "pork", "bacon", "cucumber",
         "lettuce", "chocolate", "vanilla"]


def random_food():
    return ("Food",), (("name", random.choice(FOODS)),)


def random_person():
    return ("Person",), (("name", random.choice(GIVEN_NAMES) + " " + random.choice(FAMILY_NAMES)),)


def get_random_graph(node_count, rel_count):
    people, foods, rels = set(), set(), {}

    while len(people) + len(foods) < node_count:
        if random.randint(1, 5) == 5:
            foods.add(random_food())
        else:
            people.add(random_person())

    people = list(people)
    foods = list(foods)

    while len(rels) < rel_count:
        start = random.choice(people)
        n = random.randint(1, 5)
        if n == 1:
            t = "KNOWS"
            end = random.choice(people)
            while end == start:
                end = random.choice(people)
        elif n == 2:
            t = "LIKES"
            end = random.choice(people)
            while end == start:
                end = random.choice(people)
        elif n == 3:
            t = "DISLIKES"
            end = random.choice(people)
            while end == start:
                end = random.choice(people)
        elif n == 4:
            t = "LIKES"
            end = random.choice(foods)
        elif n == 5:
            t = "DISLIKES"
            end = random.choice(foods)
        rel_key, rel_value = (start, end), (start, t, end)
        rels[rel_key] = rel_value

    nodes = [(set(node[0]), dict(node[1])) for node in people + foods]
    rels = [((rel[0][0][0], rel[0][1][0][0], rel[0][1][0][1]), rel[1], (rel[2][0][0], rel[2][1][0][0], rel[2][1][0][1])) for rel in rels.values()]
    return nodes, rels


if __name__ == "__main__":
    nodes, rels = get_random_graph(120, 400)
    for node in nodes:
        print(node)
    for rel in rels:
        print(rel)
