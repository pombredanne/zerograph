from __future__ import print_function

from time import time
import unittest
from uuid import uuid4

from .helpers import ZerographTestCase


class PointersTestCase(ZerographTestCase):

    @unittest.skip("Performance measure")
    def test_can_create_many_nodes(self):
        count = 10000
        t = [time()]
        batch = self.graph.batch()
        for i in range(count):
            batch.create_node(properties={"number": i, "uuid": uuid4().hex})
        t.append(time())
        results = batch.submit()
        result_list = list(results)
        t.append(time())
        assert len(result_list) == count
        print("Creation   : {:.3f}s/1000".format(1000 * (t[1] - t[0]) / count))
        print("Iteration  : {:.3f}s/1000".format(1000 * (t[2] - t[1]) / count))


if __name__ == "__main__":
    unittest.main()
