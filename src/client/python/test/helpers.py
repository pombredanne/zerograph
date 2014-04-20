from unittest import TestCase

from zerograph import Graph, Error


class ZerographTestCase(TestCase):

    graph = None
    host = "localhost"
    port = 47471

    @classmethod
    def setUpClass(cls):
        cls.graph = Graph.open(cls.host, cls.port)

    @classmethod
    def tearDownClass(cls):
        pass
