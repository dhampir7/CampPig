package stu.admin.event;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import stu.admin.coupon.AdminCouponService;
import stu.common.common.CommandMap;

@Controller
public class AdminEventController {
	
	Logger log = Logger.getLogger(this.getClass());

	// AdminEventService => Resource
	@Resource(name = "adminEventService")
	private AdminEventService adminEventService;
	
	// AdminCouponService => Resource
	@Resource(name = "adminCouponService")
	private AdminCouponService adminCouponService;
	
	// start    Event List View  => http://localhost:8080/stu/adminEventList.do
	@RequestMapping(value = "/adminEventList.do", method = RequestMethod.GET)
	public ModelAndView eventList(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("/event/eventList");
		return mv;
	}
	@RequestMapping(value = "/adminEventList.do", method = RequestMethod.POST)
	public ModelAndView searchEventList(CommandMap commandMap) throws Exception {
		
		ModelAndView mv = new ModelAndView("jsonView");
		List<Map<String, Object>> list = adminEventService.eventList(commandMap.getMap());
		
		mv.addObject("list", list);
		
		if(list.size() > 0) {
			mv.addObject("TOTAL", list.get(0).get("TOTAL_COUNT"));
		}
		else{
			mv.addObject("TOTAL", 0);
		}
		return mv;
	}
	// end
	////////////////////////////////////////////
	
	
	
	// start Event new InsertForm
	@RequestMapping(value = "/adminEventWriteForm.do", method = RequestMethod.GET)
	public ModelAndView eventInsertForm() throws Exception {
		ModelAndView mv = new ModelAndView("/event/eventWrite");
		return mv;
	}
	@RequestMapping(value = "/adminEventWrite.do")
	@ResponseBody
	public ModelAndView eventWrite() throws Exception {
		ModelAndView mv = new ModelAndView("jsonView");
		List<Map<String, Object>> list = adminEventService.eventNextVal();
		mv.addObject("list", list);
		return mv;
	}
	// end
	////////////////////////////////////////////
	
	
	
	// startEvent old DetailForm
	@RequestMapping(value = "/adminEventDetailForm.do", method = RequestMethod.POST)
	public ModelAndView eventDetailForm(CommandMap commandMap) throws Exception {
		ModelAndView mv = new ModelAndView("/event/eventDetail");
		return mv;
	}
	
	//Event Detail View (????????? ??????)
	@RequestMapping(value = "/adminEventDetail.do")
	@ResponseBody
	public ModelAndView eventDetail(CommandMap commandMap, HttpServletRequest request) throws Exception {
		
		Object EVENT_NO = commandMap.get("EVENT_NO");
		
		if (EVENT_NO == null || EVENT_NO == "") {
			ModelAndView mv = new ModelAndView("redirect:/adminCouponList.do");
			return mv;
		} else {
			ModelAndView mv = new ModelAndView("jsonView");
			
			List<Map<String, Object>> list = adminEventService.eventDetail(commandMap.getMap());
			mv.addObject("list", list);
			
			return mv;
		}
		
		
	}
	// end
	////////////////////////////////////////////
	
	
	//????????? ?????? ??? ????????????
	@RequestMapping(value = "/adminEventInU.do", method = RequestMethod.POST)
	public ModelAndView adminEventInU(CommandMap commandMap, HttpServletRequest request)
	throws Exception {
		
		//????????? ????????? ?????? (insert-??????, modify-??????) ?????? ???????????? ??????
		Object type = commandMap.get("TYPE");
		
		//?????? mv, ?????? ??????
		ModelAndView mv = new ModelAndView("/event/redirect");
		String msg = "", url = "", a_link_coupon_state = "";
		String gubun = (String) commandMap.get("EVENT_GUBUN"); // 0 ?????????, 1 ??????
		
		//S A????????? ???????????? ???????????? ??????
		String content = (String) commandMap.get("EVENT_CONTENT"); // ????????? ????????? ?????????
		int start_index = content.indexOf("<a href=\"/stu/couponSave.do?COUPON_NO="),   last_index = content.lastIndexOf("<a href=\"/stu/couponSave.do?COUPON_NO="), a_link_coupon_no = 0;
		if (start_index >= 0) {
			a_link_coupon_no = Integer.parseInt(content.substring(start_index, start_index+40).replaceAll("[^0-9]","")); //????????? ??????
			commandMap.put("COUPON_NO", a_link_coupon_no);
			a_link_coupon_state = adminCouponService.coupon_state(commandMap.getMap());
		}
		System.out.println("state : "+a_link_coupon_state);
		//E A????????? ???????????? ???????????? ??????
		
		//????????? type ??? ????????? insert, modify??? ?????? ?????? false ??????
		if ( ("insert".equals(type) || "modify".equals(type)) && a_link_coupon_state != null) {  //??????, ?????? ??????????????? ????????? ?????? ????????? ?????? ?????? ?????? ??????
			
			if ("end".equals(a_link_coupon_state)) {
				if ("0".equals(gubun)) { //???????????? ????????? ??????
					adminEventService.adminEventInU(commandMap.getMap(), request); //????????? ????????? a????????? ???????????? ???????????? ???????????? ?????? ????????? ??????.
					msg = "???????????????????????????";
					url = "/adminEventList.do";
				} else { //????????? ????????? ??????
					msg = "????????? ???????????? ????????? ??????url??? ?????? ???????????????.";
					if ("insert".equals(type)) { url = "/adminEventWriteForm.do"; }
					else { url = "javascript:history.back(-2)"; }
				}
			}
			else if (a_link_coupon_state != "end") {
				if (start_index == last_index) { //????????? ??????no a?????????  0~1??? ??? ?????? in/up?????? ??? ?????? msg ??????
					adminEventService.adminEventInU(commandMap.getMap(), request); //????????? ????????? ????????? ??????.
					msg = "???????????????????????????";
					url = "/adminEventList.do";
				}
				else { //????????? ??????no a????????? 2??? ????????? ??????
					msg = "?????? URL ????????? 1?????? ?????? ???????????????.";
					if ("insert".equals(type)) { url = "/adminEventWriteForm.do"; }
					else { url = "javascript:history.back(-2)"; }
				}
			}

		} else { msg = "????????? ???????????????."; }
		
		//url??? ???????????? ?????? ?????? ?????? ????????? ??????
		if ("".equals(url)) { url = "/adminEventList.do"; }
		
		mv.addObject("message", msg);
		mv.addObject("urlPage", url);
		
		return mv;
	}
	/////////////////////////////////////
	
	
	@RequestMapping(value = "/event/list.do", method = RequestMethod.GET)
	public ModelAndView eventListView()
	throws Exception {
		ModelAndView mv = new ModelAndView("event/main_eventlist");
		return mv;
	}
	@RequestMapping(value = "/event/list.do", method = RequestMethod.POST)
	public ModelAndView eventListView_POST(CommandMap commandMap, HttpServletRequest request)
	throws Exception {
		ModelAndView mv = new ModelAndView("jsonView");
		
		//????????? ????????? ????????? ???????????? ???????????? ??? ????????? ????????? ???????????? ???????????? ???????????? ??? ???????????? ??????..
		
		List<Map<String, Object>> list = adminEventService.common_eventList(commandMap.getMap());
		
		mv.addObject("list", list);
		
		if(list.size() > 0) {
			mv.addObject("TOTAL", list.get(0).get("TOTAL_COUNT"));
		}
		else{
			mv.addObject("TOTAL", 0);
		}
		return mv;
	}
	////////////////////////////////////////////
	
	
	
	@RequestMapping(value = "/event/detailViewForm.do", method = RequestMethod.POST)
	public ModelAndView detailViewForm(CommandMap commandMap, HttpServletRequest request)
	throws Exception {
		ModelAndView mv = new ModelAndView("/event/main_eventDetail");
		return mv;
	}
	
	@RequestMapping(value = "/event/detail.do")
	public ModelAndView detailView(CommandMap commandMap, HttpServletRequest request)
	throws Exception {
		
		Object EVENT_NO = commandMap.get("EVENT_NO");
		
		if (EVENT_NO == null || EVENT_NO == "") {
			ModelAndView mv = new ModelAndView("redirect:/event/list.do");
			return mv;
		} else {
			ModelAndView mv = new ModelAndView("jsonView");
			
			List<Map<String, Object>> list = adminEventService.common_eventDetail(commandMap.getMap());
			mv.addObject("list", list);
			
			return mv;
		}
		
	}
	////////////////////////////////////////////

	
}
