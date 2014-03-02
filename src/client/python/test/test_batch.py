from uuid import uuid4

from zerograph import Session


def test_single_execute():
    session = Session("tcp://localhost:47474")
    batch = session.batch()
    batch.do_execute("CREATE (a:Person {name:'Alice'}) RETURN a")
    for rs in batch.submit():
        print(rs)


def test_create_single_node():
    session = Session("tcp://localhost:47474")
    batch = session.batch()
    batch.do_create_node(["Person"], {"name": "Alice"})
    for rs in batch.submit():
        print(rs)


def test_create_a_hundred_nodes():
    session = Session("tcp://localhost:47474")
    batch = session.batch()
    for n in range(100):
        batch.do_create_node(["Number"], {"value": n})
    for rs in batch.submit():
        print(rs)


def test_create_ten_thousand_nodes():
    session = Session("tcp://localhost:47474")
    batch = session.batch()
    for n in range(10000):
        batch.do_create_node(["Number"], {"value": n, "uuid": uuid4().hex})
    for rs in batch.submit():
        print(rs)


def test_create_two_nodes_and_a_rel_100x():
    session = Session("tcp://localhost:47474")
    batch = session.batch()
    for n in range(100):
        alice = batch.do_create_node(["Person"], {"name": "Alice"})
        bob = batch.do_create_node(["Person"], {"name": "Bob"})
        batch.do_create_rel(alice, bob, "KNOWS", {"since": 1999})
    for rs in batch.submit():
        print(rs)
