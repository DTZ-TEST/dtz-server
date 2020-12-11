package com.sy.controller;

import com.sy.entity.pojo.RoomCard;
import com.sy.entity.pojo.SystemUser;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.UrlParamUtil;
import com.sy.mainland.util.cache.CacheEntity;
import com.sy.mainland.util.cache.CacheEntityUtil;
import com.sy.util.AccountUtil;
import com.sy.util.Constant;
import com.sy.util.PropUtil;
import com.sy.util.StringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/page/*" })
public class PageController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(PageController.class);

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/h5pay" })
	public String h5pay(HttpServletRequest request, HttpServletResponse response) throws Exception {
		forwardParams(request);
		return "h5Pay";
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/buy/cards" })
	public String buyCards(HttpServletRequest request, HttpServletResponse response) throws Exception {
		setSessionValue(request,"server_goods_items",PropUtil.getString("goods_items","", Constant.H5PAY_FILE));

		if ("1".equals(request.getParameter("delegate"))){
			return "buy_cards_delegate";
		}

		return "buy_cards";
	}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/manage"})
	public String goManage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "manage";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/khmanage"})
	public String gokhManage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "khmanage";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/manage/transfer"})
	public String goManageTransfer(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "agency_transfer";
	}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/manage/improve"})
	public String goManageImprove(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "agency_improve";
	}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/manage/player_pay_bind_agency/rest"})
	public String goManagePlayerResetAgency(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "pay_bind_reset";
	}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/cash"})
	public String cash(HttpServletRequest request, HttpServletResponse response) throws Exception {

		if (StringUtils.isBlank(loadRoomCard(request).getOpenid())){
			setSessionValue(request,"cash_code_state", UUID.randomUUID().toString());
		}

		return "cash";
	}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/statistics/all"})
	public String goStatisticAll(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "statistic";
	}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/forgot"})
	public String goForgot(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "forgot";
	}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/statistics/onlineData"})
	public String onlineData(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "onlinedata";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/statistics/jfonlineData"})
	public String jfonlineData(HttpServletRequest request, HttpServletResponse response) throws Exception {
		forwardParams(request);
		return "jfonlinedata";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/statistics/common"})
	public String goStatisticCards(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "statistic_common";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/statistics/jfcommon"})
	public String gojfStatisticCards(HttpServletRequest request, HttpServletResponse response) throws Exception {
		forwardParams(request);
		return "jfstatistic_common";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/statistics/cards"})
	public String goCardsStatistics(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "statistic_cards";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/statistics/jf/cards"})
	public String jfgoCardsStatistics(HttpServletRequest request, HttpServletResponse response) throws Exception {
		forwardParams(request);
		return "jfstatistic_cards";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/statistics/agent"})
	public String goAgent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "statistic_agent";
	}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/player/info"})
	public String goPlayerInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "player_info";
	}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/qrcode"})
	public String qrcode(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RoomCard roomCard=loadRoomCard(request);
		if (roomCard.getAgencyLevel()!=null&&roomCard.getAgencyLevel().intValue()==99){
//			CacheEntity<String> cache1=CacheEntityUtil.getCache("agencyId="+roomCard.getAgencyId().intValue());
			String tempAgencyId;
			int seconds;
//			if (cache1==null){
				seconds=NumberUtils.toInt(PropUtil.getString("link_seconds","600"),10*60);
				tempAgencyId=AccountUtil.loadTempAgencyId(7);
				while (true){
					String key="agencyId:"+tempAgencyId;
					if (!CacheEntityUtil.containsKey(key)){
						CacheEntityUtil.setCache(key,new CacheEntity<>(roomCard.getAgencyId(),seconds));
						break;
					}else{
						tempAgencyId = AccountUtil.loadTempAgencyId(7);
					}
				}
				CacheEntityUtil.setCache("agencyId="+roomCard.getAgencyId().intValue(),new CacheEntity<>(tempAgencyId,seconds));
//			}else{
//				tempAgencyId=cache1.getValue();
//				seconds=(int)(cache1.ttl()/1000);
//			}
			request.setAttribute("agencyId0",tempAgencyId);
			request.setAttribute("agencyId0ttl",seconds);
		}
		return "qrcode";
	}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/msg/detail"})
	public String goMyMsgDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "my_msg";
	}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/user/pwd"})
	public String goUserPwd(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "my_msg_pwd";
	}

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/user/detail"})
	public String goUserDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "my_msg_detail";
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/register" })
	public String goRegister(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "register";
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/index" })
	public String goIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "login";
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/home" })
	public String goHome(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SystemUser user = loadSystemUser(request);
		if(user.getRoleId() != null && user.getRoleId()==2){
			return "khindex";
		}
		return "index";
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/player_pay" })
	public String goPlayerPay(HttpServletRequest request, HttpServletResponse response) throws Exception {
		forwardParamsOfDate(request);
		return "player_pay";
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/agency_pay" })
	public String goAgencyPay(HttpServletRequest request, HttpServletResponse response) throws Exception {
		forwardParamsOfDate(request);
		return "agency_pay";
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/pay/history/player" })
	public String goPlayerPayHistory(HttpServletRequest request, HttpServletResponse response) throws Exception {
		forwardParamsOfDate(request);
		return "pay_player";
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/pay/history/agency" })
	public String goAgencyPayHistory(HttpServletRequest request, HttpServletResponse response) throws Exception {
		forwardParamsOfDate(request);
		return "pay_agency";
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/pay_history" })
	public String goPayHistory(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "pay_history";
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/income" })
	public String goIncome(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String appid=PropUtil.getString("appid");
		String code=UUID.randomUUID().toString();
		request.setAttribute("wx_appid",appid);
		setSessionValue(request,"cash_code_state",code);
		return "income";
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/player" })
	public String goPlayer(HttpServletRequest request, HttpServletResponse response) throws Exception {
		forwardParams(request);
		return "player";
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/agency" })
	public String goAgency(HttpServletRequest request, HttpServletResponse response) throws Exception {
		forwardParams(request);
		return "agency";
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/agency1" })
	public String goAgency1(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "agency1";
	}
	private static final void forwardParams(HttpServletRequest request) throws Exception{
		Map<String,String> params=UrlParamUtil.getParameters(request);
		for(Map.Entry<String,String> kv:params.entrySet()){
			request.setAttribute(kv.getKey(),kv.getValue());
		}
	}

	private static final void forwardParamsOfDate(HttpServletRequest request) throws Exception{
		String date=request.getParameter("date");
		if(StringUtils.isNotBlank(date)){
			String startDate,endDate;
			switch (date){
				case "today":
					startDate=CommonUtil.dateTimeToString("yyyy-MM-dd");
					endDate=startDate;
					break;
				case "week":
					String[] strs1=StringUtil.loadWeekRange(new Date());
					startDate=strs1[0];
					endDate=strs1[1];
					break;
				case "month":
					String[] strs2=StringUtil.loadMonthRange(new Date());
					startDate=strs2[0];
					endDate=strs2[1];
					break;
				default:
					startDate=CommonUtil.dateTimeToString("yyyy-MM-dd");
					endDate=startDate;
			}
			request.setAttribute("startDate",startDate);
			request.setAttribute("endDate",endDate);
		}
	}
	
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/userPurchase"})
	public String goUserPurchase(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "user_purchase";
	}
	
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agencyPurchase"})
	public String agencyPurchase(HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setAttribute("server_goods_items",PropUtil.getString("goods_items","", Constant.H5PAY_FILE));
		return "agencyPurchase";
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/pay/for/player" })
	public String goPayForPlayer(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "pay_for_player";
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/pay/for/agency" })
	public String goPayForAgency(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "pay_for_agency";
	}
	
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/manage/agency/rest"})
	public String restAgencyInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "rest_agency";
	}
	
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/pay/addOrder"})
	public String addOrder(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "addOrder";
	}
	
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/pay/offlinePay"})
	public String offlinePay(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "offlinePay";
	}
	
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/pay/info" })
	public String goPayInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		forwardParams(request);
		return "pay_info";
	}
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/buy/card/info" })
	public String buycardInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		forwardParams(request);
		return "buycard_info";
	}
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/manage/buy/card/info" })
	public String mbuycardInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		forwardParams(request);
		return "mbuycard_info";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/black/forbid"})
	public String goUserForbid(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "user_forbid";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agency/forbid"})
	public String goAgencyForbid(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "agency_forbid";
	}
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/d/pay/history/agency" })
	public String godAgencyPayHistory(HttpServletRequest request, HttpServletResponse response) throws Exception {
		forwardParamsOfDate(request);
		return "d_pay_agency";
	}
	
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/manage/back/cards"})
	public String backCards(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "back_cards";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/manage/vip/wx"})
	public String vipwx(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "vipwx";
	}
	
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/agency/query"})
	public String agencyQuery(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "agency_query";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/unroom"})
	public String unroom(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "unroom";
	}
	
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/manage/cashresend" })
	public String cashresend(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "cashresend";
	}
	
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/manage/activityreward" })
	public String activityreward(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "activityreward";
	}
	
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/queryGroupByplayerId" })
	public String queryGroupByplayerId(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "queryGroupByplayerId";
	}
	
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/queryAgencyPay" })
	public String queryagencypay(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "queryagencypay";
	}
	
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/queryAgencyPaySource" })
	public String queryAgencyPaySource(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "queryAgencyPaySource";
	}
	
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = { "/agencyCard" })
	public String agencyCard(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "agencyCard";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/blacks/groupmanage"})
	public String goAddGroupManager(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "addGroupManager";
	}
	
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/jtgroup"})
	public String gojtGroup(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "jtgroup";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/pkdata"})
	public String pkdata(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "pkdata";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/paomadeng"})
	public String paomadeng(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "paomadeng";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/newDau"})
	public String newDau(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "newDau";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/jl"})
	public String jl(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "jl";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/whitemenu"})
	public String whitemenu(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "whitemenu";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/delgoldroom"})
	public String delgoldroom(HttpServletRequest request, HttpServletResponse response) throws Exception {

		return "delgoldroom";
	}
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/transferLeader"})
	public String transferLeader(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return "transferLeader";
	}

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"/bindPhone"})
    public String bindPhone(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return "bindPhone";
    }
}

