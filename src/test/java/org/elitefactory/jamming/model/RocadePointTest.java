package org.elitefactory.jamming.model;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class RocadePointTest {

	@Test
	public void test() {

		List<RocadePoint> points = RocadePoint.getPointsForDirection(RocadeDirection.outer);
		Assert.assertNotNull(points);
		Assert.assertEquals(31, points.size());
		Assert.assertEquals(RocadePoint.O01, points.get(0));
		Assert.assertEquals(RocadePoint.O02, points.get(1));
		Assert.assertEquals(RocadePoint.O03, points.get(2));
		Assert.assertEquals(RocadePoint.O04, points.get(3));
		Assert.assertEquals(RocadePoint.O26, points.get(25));

		points = RocadePoint.getPointsForDirection(RocadeDirection.inner);
		Assert.assertNotNull(points);
		Assert.assertEquals(30, points.size());
	}

}
