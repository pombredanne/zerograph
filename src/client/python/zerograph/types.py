
import json

class Entity(object):

    def __init__(self, attributes):
        self.__id = attributes.get("id")

    def __repr__(self):
        return "<Entity id={0}>".format(self._id)

    @property
    def _id(self):
        return self.__id


class PropertyContainer(Entity):

    def __init__(self, attributes):
        Entity.__init__(self, attributes)
        self.__properties = dict(attributes.get("properties", {}))

    def __repr__(self):
        return "<PropertyContainer id={0} properties={1}>".format(self._id, self.properties)

    @property
    def properties(self):
        return self.__properties


class Node(PropertyContainer):

    def __init__(self, attributes):
        PropertyContainer.__init__(self, attributes)
        self.__labels = set(attributes.get("labels", set()))

    def __repr__(self):
        return "<Node id={0} labels={1}, properties={2}>".format(self._id, self.labels, self.properties)

    @property
    def labels(self):
        return self.__labels


class Rel(PropertyContainer):

    def __init__(self, attributes):
        PropertyContainer.__init__(self, attributes)
        self.__start = Node(attributes["start"])
        self.__end = Node(attributes["end"])
        self.__type = attributes["type"]

    def __repr__(self):
        return "<Rel id={0} start={1} end={2} type={3}, properties={4}>".format(self._id, self.start, self.end, self.labels, self.properties)

    @property
    def start(self):
        return self.__start

    @property
    def end(self):
        return self.__end

    @property
    def type(self):
        return self.__type


def hydrate(string):
    if string.startswith("/*"):
        cls, string = string[2:].partition("*/")[0::2]
        value = json.loads(string)
        if cls == "Node":
            return Node(value)
        elif cls == "Rel":
            return Rel(value)
        elif cls == "Path":
            return Path(value)
        else:
            return value
    else:
        return json.loads(string)
