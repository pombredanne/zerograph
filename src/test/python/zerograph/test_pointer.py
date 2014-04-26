import unittest

from zerograph import Batch

from .helpers import ZerographTestCase


class PointersTestCase(ZerographTestCase):

    def test_can_use_pointers(self):
        batch = Batch(self.graph)
        a = batch.create_node()
        b = batch.create_node()
        batch.create_rel(a, b, "KNOWS")
        results = batch.submit()
        for result in results:
            print(result)
        # TODO: assertions


if __name__ == "__main__":
    unittest.main()
