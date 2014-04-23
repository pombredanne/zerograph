from unittest import main, TestCase

import yaml

from zerograph import Batch, Rel

from .helpers import ZerographTestCase


class RelFromYamlTestCase(TestCase):

    def test_can_hydrate_rel_with_type(self):
        hydrated = yaml.load('!Rel {"type":"KNOWS"}')
        assert hydrated == Rel("KNOWS")
        assert not hydrated.bound

    def test_can_hydrate_rel_with_type_and_properties(self):
        hydrated = yaml.load('!Rel {"type":"KNOWS","properties":{"since":1999}}')
        assert hydrated == Rel("KNOWS", since=1999)
        assert not hydrated.bound


class RelCastTestCase(TestCase):

    def test_casting_none_will_return_none(self):
        casted = Rel.cast(None)
        assert casted is None

    def test_casting_rel_will_return_same(self):
        rel = Rel("KNOWS", since=1999)
        casted = Rel.cast(rel)
        assert casted is rel

    def test_casting_string_will_return_rel_with_type(self):
        type = "KNOWS"
        casted = Rel.cast(type)
        assert casted == Rel(type)

    def test_casting_string_and_dict_will_return_rel_with_properties(self):
        type = "KNOWS"
        properties = {"since": 1999}
        casted = Rel.cast((type, properties))
        assert casted == Rel(type, **properties)


class RelConstructionTestCase(TestCase):

    def test_can_construct_rel_with_type(self):
        rel = Rel("KNOWS")
        assert rel.type == "KNOWS"
        assert rel.properties == {}

    def test_can_construct_rel_with_type_and_properties(self):
        rel = Rel("KNOWS", since=1999)
        assert rel.type == "KNOWS"
        assert rel.properties == {"since": 1999}


class RelRepresentationTestCase(TestCase):

    def test_can_represent_rel_with_type(self):
        rel = Rel("KNOWS")
        string = repr(rel)
        assert string == '-[:KNOWS]->'

    def test_can_represent_rel_with_type_and_properties(self):
        rel = Rel("KNOWS", since=1999)
        string = repr(rel)
        assert string == '-[:KNOWS {since:1999}]->'


class RelEqualityTestCase(TestCase):

    def test_equal_rels(self):
        rel_1 = Rel("KNOWS", since=1999)
        rel_2 = Rel("KNOWS", since=1999)
        assert rel_1 == rel_2

    def test_unequal_rels(self):
        rel_1 = Rel("KNOWS", since=1999)
        rel_2 = Rel("KNOWS", since=2000)
        assert rel_1 != rel_2


class RelTypeTestCase(TestCase):

    def test_rel_type(self):
        rel = Rel("KNOWS")
        assert rel.type == "KNOWS"


class RelExistsTestCase(ZerographTestCase):

    def test_rel_exists(self):
        batch = self.graph.batch()
        a = batch.create_node()
        b = batch.create_node()
        ab = batch.create_rel(a, b, "KNOWS")
        result = batch.submit()
        alice = next(result)
        bob = next(result)
        alice_bob = next(result)
        alice_bob_rel = alice_bob.rels[0]
        assert alice_bob_rel.exists

    def test_node_does_not_exist(self):
        batch = self.graph.batch()
        a = batch.create_node()
        b = batch.create_node()
        ab = batch.create_rel(a, b, "KNOWS")
        result = batch.submit()
        alice = next(result)
        bob = next(result)
        alice_bob = next(result)
        alice_bob_rel = alice_bob.rels[0]
        Batch.single(self.graph, Batch.delete_rel, alice_bob_rel.bound_id)
        assert not alice_bob_rel.exists


class RelPullTestCase(ZerographTestCase):

    def test_remote_rel_changes_can_be_pulled(self):
        batch = self.graph.batch()
        a = batch.create_node()
        b = batch.create_node()
        ab = batch.create_rel(a, b, "KNOWS")
        result = batch.submit()
        alice = next(result)
        bob = next(result)
        alice_bob = next(result)
        alice_bob_rel = alice_bob.rels[0]
        local = Rel("KNOWS")
        local.bind(self.graph, alice_bob_rel.bound_id)
        local.pull()
        assert local.type == alice_bob_rel.type
        assert local.properties == alice_bob_rel.properties


class RelPushTestCase(ZerographTestCase):

    def test_local_rel_changes_can_be_pushed(self):
        batch = self.graph.batch()
        a = batch.create_node()
        b = batch.create_node()
        ab = batch.create_rel(a, b, "KNOWS")
        result = batch.submit()
        alice = next(result)
        bob = next(result)
        alice_bob = next(result)
        alice_bob_rel = alice_bob.rels[0]
        local = Rel("KNOWS", since=1999)
        local.bind(self.graph, alice_bob_rel.bound_id)
        local.push()
        remote_path = Batch.single(self.graph, Batch.get_rel, local.bound_id)
        remote_rel = remote_path.rels[0]
        assert remote_rel.type == local.type
        assert remote_rel.properties == local.properties


if __name__ == "__main__":
    main()

