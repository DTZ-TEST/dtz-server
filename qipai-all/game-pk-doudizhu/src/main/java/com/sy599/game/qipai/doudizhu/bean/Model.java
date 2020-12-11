package com.sy599.game.qipai.doudizhu.bean;

import java.util.List;
import java.util.Vector;

public class Model {
	/** 手数 */
	int count;//手数
	/** 手数 */
	int value;//权值
	//一组牌
	/** 单张 */
	public List<String> a1=new Vector<>(); //单张
	/** 对子 */
	public List<String> a2=new Vector<>(); //对子
	/** 3带 */
	public List<String> a3=new Vector<>(); //3带
	/** 连子 */
	public List<String> a123=new Vector<>(); //连子
	/** 连牌 */
	public List<String> a112233=new Vector<>(); //连牌
	/** 飞机 */
	public List<String> a111222=new Vector<>(); //飞机
	/** 炸弹 */
	public List<String> a4=new Vector<>(); //炸弹
	/** 王炸*/
	public List<String> a1617=new Vector<>();

	/** 3带1*/
	public List<String> a31 = new Vector<>();
	/** 3带一对*/
	public List<String> a32 = new Vector<>();
	/** 4带2*/
	public List<String> a411 = new Vector<>();
	/** 4带二对*/
	public List<String> a422 = new Vector<>();
	/** 飞机带单*/
	public List<String> a11122234 = new Vector<>();
	/** 飞机带双*/
	public List<String> a1112223344 = new Vector<>();
}
