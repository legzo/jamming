package org.elitefactory.jamming.model;

import java.util.ArrayList;
import java.util.List;

public enum RocadePoint {

	O01(213, 343), O02(221, 341), O03(229, 341), O04(237, 342), O05(245, 344), O06(253, 346), O07(261, 350), O08(269,
			355), O09(277, 360), O10(285, 364), O11(293, 366), O12(301, 370), O13(309, 372), O14(317, 374), O15(325,
			376), O16(333, 377), O17(341, 377), O18(349, 377), O19(357, 376), O20(365, 374), O21(373, 369), O22(379,
			361), O23(382, 354), O24(379, 361), O25(384, 347), O26(385, 340), O27(387, 333), O28(390, 324), O29(393,
			317), O30(397, 311), O31(401, 306),

	I01(213, 335), I02(221, 334), I03(229, 334), I04(237, 334), I05(245, 336), I06(253, 338), I07(261, 341), I08(269,
			345), I09(277, 350), I10(285, 355), I11(293, 359), I12(301, 361), I13(309, 364), I14(317, 367), I15(325,
			368), I16(333, 369), I17(341, 370), I18(349, 369), I19(357, 368), I20(365, 365), I21(371, 358), I22(375,
			348), I23(378, 339), I24(379, 332), I25(382, 323), I26(385, 315), I27(389, 309), I28(393, 304), I29(397,
			299), I30(401, 296);

	public int x;
	public int y;

	private RocadePoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public static List<RocadePoint> getPointsForDirection(RocadeDirection direction) {
		List<RocadePoint> points = new ArrayList<RocadePoint>();

		for (RocadePoint point : RocadePoint.values()) {
			if (point.name().toLowerCase().startsWith(direction.name().substring(0, 1))) {
				points.add(point);
			}
		}

		return points;
	}
}
