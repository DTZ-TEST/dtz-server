package com.sy599.game.qipai.doudizhu;

import com.sy599.game.qipai.doudizhu.tool.CardTypeTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DdzTest {
	public static void main(String[] args) {
//		List<Integer> list1 = new ArrayList<>(Arrays.asList(105,305,306,206,205,406));
//		List<Integer> list2 = new ArrayList<>(Arrays.asList(207,408,307,107,308,108));
//		List<Integer> list3 = new ArrayList<>();
//		list3.add(1);
//		list3.add(2);
//		list3.add(3);
//		List<Integer> list4 = new ArrayList<>();
//		list4.add(2);
//		list4.add(4);
//		list4.add(5);
//		list3.removeAll(list4);
//		System.out.println(list3);
		List<Integer> list= new ArrayList<>(Arrays.asList(114,214,314,111,110));
		List<Integer> tempList = CardTypeTool.getMaxThreeLian(list,1);
		System.out.println(tempList);
		System.out.println(CardTypeTool.getTwo(list,1,tempList));



	}
}
