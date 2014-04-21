from unittest import main, TestCase

import yaml

from zerograph import Batch, Node, Pointer

from ..helpers import ZerographTestCase


class NodeFromYamlTestCase(TestCase):

    def test_can_hydrate_empty_node(self):
        hydrated = yaml.load('!Node {}')
        assert hydrated == Node()
        assert not hydrated.bound

    def test_can_hydrate_node_with_labels(self):
        hydrated = yaml.load('!Node {"labels":["Human","Female"]}')
        assert hydrated == Node("Human", "Female")
        assert not hydrated.bound

    def test_can_hydrate_node_with_properties(self):
        hydrated = yaml.load('!Node {"properties":{"name":"Alice","age":33}}')
        assert hydrated == Node(name="Alice", age=33)
        assert not hydrated.bound

    def test_can_hydrate_node_with_labels_and_properties(self):
        hydrated = yaml.load('!Node {"labels":["Human","Female"],'
                             '"properties":{"name":"Alice","age":33}}')
        assert hydrated == Node("Human", "Female", name="Alice", age=33)
        assert not hydrated.bound


class NodeCastTestCase(TestCase):

    def test_casting_none_will_return_none(self):
        casted = Node.cast(None)
        assert casted is None

    def test_casting_node_will_return_same(self):
        node = Node("Person", name="Alice")
        casted = Node.cast(node)
        assert casted is node

    def test_casting_string_will_return_node_with_label(self):
        label = "Person"
        casted = Node.cast(label)
        assert casted == Node(label)

    def test_casting_dict_will_return_node_with_properties(self):
        properties = {"name": "Alice"}
        casted = Node.cast(properties)
        assert casted == Node(**properties)


class NodeConstructionTestCase(TestCase):

    def test_can_construct_empty_node(self):
        node = Node()
        assert node.labels == set()
        assert node.properties == {}

    def test_can_construct_node_with_labels(self):
        node = Node("Human", "Female")
        assert node.labels == {"Human", "Female"}
        assert node.properties == {}

    def test_can_construct_node_with_properties(self):
        node = Node(name="Alice", age=33)
        assert node.labels == set()
        assert node.properties == {"name": "Alice", "age": 33}

    def test_can_construct_node_with_labels_and_properties(self):
        node = Node("Human", "Female", name="Alice", age=33)
        assert node.labels == {"Human", "Female"}
        assert node.properties == {"name": "Alice", "age": 33}


class NodeRepresentationTestCase(TestCase):

    def test_can_represent_empty_node(self):
        node = Node()
        string = repr(node)
        assert string == '()'

    def test_can_represent_node_with_labels(self):
        node = Node("Human", "Female")
        string = repr(node)
        assert string == '(:Female:Human)'

    def test_can_represent_node_with_properties(self):
        node = Node(name="Alice", age=33)
        string = repr(node)
        assert string == '({age:33,name:"Alice"})'

    def test_can_represent_node_with_labels_and_properties(self):
        node = Node("Human", "Female", name="Alice", age=33)
        string = repr(node)
        assert string == '(:Female:Human {age:33,name:"Alice"})'


class NodeEqualityTestCase(TestCase):

    def test_equal_nodes(self):
        node_1 = Node("Person", name="Alice")
        node_2 = Node("Person", name="Alice")
        assert node_1 == node_2

    def test_unequal_nodes(self):
        node_1 = Node("Person", name="Alice")
        node_2 = Node("Person", name="Bob")
        assert node_1 != node_2


class NodeLabelsTestCase(TestCase):

    def test_single_node_label(self):
        node = Node("Person", name="Alice")
        assert node.labels == {"Person"}

    def test_multiple_node_labels(self):
        node = Node("Human", "Female", name="Alice")
        assert node.labels == {"Human", "Female"}

    def test_duplicate_node_labels(self):
        node = Node("Human", "Human", name="Alice")
        assert node.labels == {"Human"}

    def test_adding_node_label(self):
        node = Node("Person", name="Alice")
        node.labels.add("Employee")
        assert node.labels == {"Person", "Employee"}

    def test_remove_node_label(self):
        node = Node("Human", "Female", name="Alice")
        node.labels.remove("Female")
        assert node.labels == {"Human"}


class NodeExistsTestCase(ZerographTestCase):

    def test_node_exists(self):
        batch = self.graph.batch()
        batch.create_node()
        result = batch.submit()
        node = next(result)
        assert node.exists

    def test_node_does_not_exist(self):
        batch = self.graph.batch()
        batch.create_node()
        batch.delete_node(Pointer(0))
        result = batch.submit()
        node = next(result)
        assert not node.exists


class NodePullTestCase(ZerographTestCase):

    def test_remote_node_changes_can_be_pulled(self):
        batch = self.graph.batch()
        batch.create_node({"Person"}, {"name": "Alice"})
        result = batch.submit()
        remote = next(result)
        local = Node()
        local.bind(self.graph, remote.bound_id)
        local.pull()
        assert local.labels == remote.labels
        assert local.properties == remote.properties


class NodePushTestCase(ZerographTestCase):

    def test_local_node_changes_can_be_pushed(self):
        batch = self.graph.batch()
        batch.create_node()
        result = batch.submit()
        remote = next(result)
        local = Node("Person", name="Alice")
        local.bind(self.graph, remote.bound_id)
        local.push()
        remote = Batch.single(self.graph, Batch.get_node, local.bound_id)
        assert remote.labels == local.labels
        assert remote.properties == local.properties


if __name__ == "__main__":
    main()

