from unittest import main, TestCase

import yaml

from zerograph import Node


class NodeFromYamlTestCase(TestCase):

    def test_can_hydrate_empty_node(self):
        hydrated = yaml.load('!Node {}')
        assert hydrated == Node()
        assert not hydrated.linked

    def test_can_hydrate_node_with_labels(self):
        hydrated = yaml.load('!Node {"labels":["Human","Female"]}')
        assert hydrated == Node("Human", "Female")
        assert not hydrated.linked

    def test_can_hydrate_node_with_properties(self):
        hydrated = yaml.load('!Node {"properties":{"name":"Alice","age":33}}')
        assert hydrated == Node(name="Alice", age=33)
        assert not hydrated.linked

    def test_can_hydrate_node_with_labels_and_properties(self):
        hydrated = yaml.load('!Node {"labels":["Human","Female"],'
                             '"properties":{"name":"Alice","age":33}}')
        assert hydrated == Node("Human", "Female", name="Alice", age=33)
        assert not hydrated.linked


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

    def test_can_construct_node_with_labels(self):
        node = Node("Human", "Female")
        string = repr(node)
        assert string == '(:Female:Human)'

    def test_can_construct_node_with_properties(self):
        node = Node(name="Alice", age=33)
        string = repr(node)
        assert string == '({"age":33,"name":"Alice"})'

    def test_can_construct_node_with_labels_and_properties(self):
        node = Node("Human", "Female", name="Alice", age=33)
        string = repr(node)
        assert string == '(:Female:Human {"age":33,"name":"Alice"})'


if __name__ == "__main__":
    main()

