package asset.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import jb.pageModel.DataGrid;
import jb.pageModel.Json;
import jb.pageModel.PageHelper;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import asset.model.AssetAttr;
import asset.model.AssetBaseInfo;
import asset.model.AssetExtInfo;
import asset.model.AssetInfo;
import asset.service.AssetBaseServiceI;
import asset.service.AssetDicServiceI;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@Controller
@RequestMapping("/ledger")
public class LedgerDetailController {
	
	
	@Autowired
	private AssetBaseServiceI assetBaseService;
	
	@Autowired
	private AssetDicServiceI assetDicService;

	
	@RequestMapping("/detail")
	public String toasset(HttpServletRequest request) {
		setColumns(request);
		return "/assets/ledger_detail";
	}
	
	@RequestMapping("/property/{assetId}")
	public String ledgerPro(HttpServletRequest request, @PathVariable String assetId) {
		request.setAttribute("assetId", assetId);
		return "/assets/ledger_pro";
	}
	

	@RequestMapping("/toAdd")
	public String toAdd(HttpServletRequest request) {
		return "/assets/ledger_toadd";
	}
	
	@RequestMapping("/add/{type}")
	public String ledgerAdd(HttpServletRequest request, @PathVariable Integer type) {
		try {
			Map<String, String> dicMap = assetDicService.getAssetDicMap(type);
			if(MapUtils.isEmpty(dicMap)){
				throw new IllegalArgumentException("param error");
			}
			request.setAttribute("dicMap", dicMap);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "/assets/ledger_add";
	}
	
	private void setColumns(HttpServletRequest request){
		String baseids = request.getParameter("baseids");
		String attrids = request.getParameter("attrids");
		StringBuilder columns = new StringBuilder("[[");
		try {
		
			if(StringUtils.isNotBlank(attrids)){
				String[] attrIdsArray = attrids.split(",");
				if(!ArrayUtils.isEmpty(attrIdsArray)){
					Map<String, String> attrMap = assetBaseService.getAllAttr();
					//获取扩展属性信息
					for(String attrId:attrIdsArray){
						if(attrMap.containsKey(attrId)){
							String attrName = attrMap.get(attrId);
							String column = "{field:'"+attrName+"',title:'"+attrName+"'},";
							columns.append(column);
						}
					}
				}
			}
			
			//默认情况 显示所有的基本信息
//			if(columns.length() == 2){
			Map<String,String> dicMap = assetDicService.getAssetDicMap(1);
			//默认选中的属性
			if(StringUtils.isBlank(baseids)){
				Properties properties = PropertiesLoaderUtils.loadAllProperties("config.properties");
				baseids = properties.getProperty("search.result.columns");
			}
			for(Map.Entry<String, String> entry:dicMap.entrySet()){
					String column = "";
					String[] baseIdsArray = baseids.split(",");
					//只显示这些列
					boolean hidden = true;
					for(String baseId:baseIdsArray){
						if(baseId.equals(entry.getKey())){
							hidden = false;
							break;
						}
					}
					if(hidden){
						column = "{field:'"+entry.getKey()+"',title:'"+entry.getValue()+"',hidden:'true'},";
					}else{
						column = "{field:'"+entry.getKey()+"',title:'"+entry.getValue()+"'},";
					}
					columns.append(column);
			}
			request.setAttribute("columns", columns.deleteCharAt(columns.length()-1)+"]]");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private JSONArray parseBase(AssetBaseInfo info) throws Exception{
		JSONArray resultArray = new JSONArray();
		Map<String,String> dicMap = assetDicService.getAssetDicMap(1);
		JSONObject infoJson = JSONObject.parseObject(JSON.toJSONString(info));
		Iterator<String> iterator = infoJson.keySet().iterator();
		while(iterator.hasNext()){
			String key = iterator.next();
			String value = infoJson.getString(key);
			JSONObject json = new JSONObject();
			if(dicMap.containsKey(key)){
				json.put("name", dicMap.get(key));
				json.put("value", value);
				json.put("key", key);
				json.put("flag", "base");
				json.put("editor", "text");
				resultArray.add(json);
			}
		}
		return resultArray;
	}
	
	@RequestMapping("/pro")
	@ResponseBody
	public DataGrid ledgerDataPro(HttpServletRequest request, PageHelper ph) {
		DataGrid dataGrid = new DataGrid();
		String assetId = request.getParameter("assetId");
		try {
				if(StringUtils.isBlank(assetId)){
					throw new IllegalArgumentException("param error");
				}
				String rulesStr = request.getParameter("assetId");
				AssetInfo assetInfo = assetBaseService.getAssetInfo(Integer.parseInt(rulesStr));
				if(null == assetInfo){
					throw new IllegalArgumentException("param error");
				}
				AssetBaseInfo info = assetInfo.getBaseInfo();
				List<AssetExtInfo> extList = assetInfo.getExtList();
				JSONArray resultArray = parseBase(info);
				if(CollectionUtils.isNotEmpty(extList)){
					for(AssetExtInfo ext:extList){
						JSONObject json = new JSONObject();
						json.put("name", ext.getAssetAttrName());
						json.put("value", ext.getAssetAttrValue());
						json.put("key", ext.getAssetAttrId());
						json.put("flag", "ext");
						json.put("editor", "text");
						resultArray.add(json);
					}
				}
				dataGrid.setRows(resultArray);
				dataGrid.setTotal(Long.valueOf(resultArray.size()));
				return dataGrid;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataGrid;
	}
	
	@RequestMapping("/data")
	@ResponseBody
	public DataGrid ledgerData(HttpServletRequest request, PageHelper ph) {
		
		System.out.println("inininininininininininininininininininininininininin");
		DataGrid dataGrid = new DataGrid();
		List<AssetInfo> assetList = null;
		try {
			
			String rulesStr = request.getParameter("filterRules");
			HashMap<String,String> baseMap = new HashMap<String,String>();
			HashMap<String,String> extMap = new HashMap<String,String>();
			if(StringUtils.isNotBlank(rulesStr)){
				JSONArray rules = JSONArray.parseArray(rulesStr);
				Map<String,String> dicMap = assetDicService.getAssetDicMap(1);
				for(int i = 0;i<rules.size();i++){
					JSONObject jsonObject = rules.getJSONObject(i);
					
					
					String field = jsonObject.getString("field");
					String value = jsonObject.getString("value");
//					jsonObject.getString("op");
					if(dicMap.containsKey(field)){
						baseMap.put(field, value);
					}else{
						extMap.put(field, value);
					}
				}
			}
			if(extMap.size() > 0){
				assetList = assetBaseService.getAssetList(baseMap,extMap,ph);	
			}else{
				assetList = assetBaseService.getAssetList(baseMap,ph);	
			}
			if(null != assetList && assetList.size() > 0){
				JSONArray rows = new JSONArray();
				for(AssetInfo asset:assetList){
					AssetBaseInfo baseInfo = asset.getBaseInfo();
					JSONObject json;
					if(null != baseInfo){
						json = JSON.parseObject(JSON.toJSONString(baseInfo));
					}else{
						json = new JSONObject();
					}
					
					List<AssetExtInfo> extList = asset.getExtList();
					
					if(null != extList && extList.size() > 0){
						for(AssetExtInfo extInfo:extList){
							String attrName = extInfo.getAssetAttrName();
							String attrValue = extInfo.getAssetAttrValue();
							json.put(attrName, attrValue);
						}
					}
					rows.add(json);
				}
				
				dataGrid.setRows(rows);
				if(extMap.size() > 0){
					
				}else{
					dataGrid.setTotal(assetBaseService.countAsset(baseMap));	
				}
				
				return dataGrid;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("end");
		return dataGrid;
	}
	
	
	@RequestMapping("/toHigh")
	public String toHigh(HttpServletRequest request) {
		try {
			//默认属性
			Map<String,String> baseAttrMap = assetDicService.getAssetDicMap(1);
			request.setAttribute("baseAttrMap", baseAttrMap);
			
			//默认选中的属性
			Properties properties = PropertiesLoaderUtils.loadAllProperties("config.properties");
			String columns = properties.getProperty("search.result.columns");
			request.setAttribute("columns", columns);
			
			//其他属性
			Map<String, List<AssetAttr>> attrByCate = assetBaseService.getAllAttrByCate();
			request.setAttribute("attrMap", attrByCate);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "/assets/ledger_high";
	}
	
	@RequestMapping("/update")
	@ResponseBody
	public Json updateAsset(HttpServletRequest request) {
		Json j = new Json();
		try {
			String assetId = request.getParameter("assetId");
			if(StringUtils.isBlank(assetId)){
				throw new IllegalArgumentException("param error");
			}
			//变更的基本信息
			String base = request.getParameter("b");
			Map<String,String> paramMap = new HashMap<String,String>();
			if(StringUtils.isNotBlank(base)){
				String[] bases = base.split(",");
				for(String param:bases){
					String[] params = param.split(":");
					if(params.length == 2){
						paramMap.put(params[0], params[1]);
					}else{
						paramMap.put(params[0], "");
					}
				}
				AssetBaseInfo baseInfo = new AssetBaseInfo();
				BeanUtils.populate(baseInfo, paramMap);
				baseInfo.setAssetId(Integer.parseInt(assetId));
				assetBaseService.updateAssetById(baseInfo);
			    }
				//变更的扩展信息
				String ext = request.getParameter("e");
				if(StringUtils.isNotBlank(ext)){
					String[] exts = ext.split(",");
					for(String param:exts){
						String[] params = param.split(":");
						AssetExtInfo extInfo = new AssetExtInfo();
						extInfo.setAssetId(Integer.parseInt(assetId));
						if(params.length == 2){
							extInfo.setAssetAttrId(Integer.parseInt(params[0]));
							extInfo.setAssetAttrValue(params[1]);
						}else{
							extInfo.setAssetAttrId(Integer.parseInt(params[0]));
							extInfo.setAssetAttrValue("");
						}
						assetBaseService.updateAssetExtById(extInfo);
					}
				}
				j.setSuccess(true);
		} catch (Exception e) {
			e.printStackTrace();
			j.setSuccess(false);
		} 
		return j;
	}
	
}