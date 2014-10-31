from unittest import main

from zerograph import BatchPush

from .helpers import ZerographTestCase


class BatchPushTestCase(ZerographTestCase):

    def test_can_batch_push_nodes_and_rel(self):
        # Create and retrieve a couple of remote nodes and a rel
        result = self.graph.execute("""\
        CREATE (a:Person {name:'Alice'})-[ab:KNOWS]->(b:Person {name:'Bob'})
        RETURN a, ab, b
        """)
        a, ab, b = next(iter(result))
        # Check the name properties of the local objects
        assert a["name"] == "Alice"
        assert b["name"] == "Bob"
        assert ab.type == "KNOWS"
        assert ab["since"] is None
        # Change the local copies
        a["name"] = "Alice Smith"
        b["name"] = "Bob Jones"
        # Batch push the local changes
        batch = BatchPush(self.graph)
        batch.add(a)
        batch.add(b)
        batch.add(ab)
        batch.submit()
        # Fetch the remote copies
        a2 = self.graph.node(a._id)
        b2 = self.graph.node(b._id)
        ab2 = self.graph.relationship(ab._id)
        # Check the remote copies match the local ones
        assert a2 == a
        assert b2 == b
        assert ab2 == ab


if __name__ == "__main__":
    main()
