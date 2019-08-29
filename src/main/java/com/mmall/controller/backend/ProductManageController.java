package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/product")
public class ProductManageController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;


    @RequestMapping(value = "save_or_edit_product.do")
    @ResponseBody
    public ServerResponse saveOrEditProduct (HttpSession session, Product product){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iProductService.saveOrEditProduct(product);
        }
        return ServerResponse.createByErrorMessage("无权限操作，请以管理员登陆");
    }

    @RequestMapping(value = "set_product_status.do")
    @ResponseBody
    public ServerResponse setProductStatus (HttpSession session, Integer productId,Integer status){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iProductService.setProductStatus(productId,status);
        }
        return ServerResponse.createByErrorMessage("无权限操作，请以管理员登陆");
    }

    @RequestMapping(value = "details.do")
    @ResponseBody
    public ServerResponse getDetail (HttpSession session, Integer productId){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iProductService.getDetail(productId);
        }
        return ServerResponse.createByErrorMessage("无权限操作，请以管理员登陆");
    }

    @RequestMapping(value = "list.do")
    @ResponseBody
    public ServerResponse getList (HttpSession session,
                                   @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iProductService.getList(pageNum,pageSize);
        }
        return ServerResponse.createByErrorMessage("无权限操作，请以管理员登陆");
    }

    @RequestMapping(value = "search.do")
    @ResponseBody
    public ServerResponse search (HttpSession session,String productName,Integer productId,
                                   @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iProductService.search(productName,productId,pageNum,pageSize);
        }
        return ServerResponse.createByErrorMessage("无权限操作，请以管理员登陆");
    }

    /**
     * 图片上传
     * session 传User chechkAdmin
     *
     * 理解：
     *
     * 有一个文件，abc.jpd
     *
     *      上传过程：file 文件和path上传的路径
     *              文件名处理：UUID加扩展名
     *              地址获取当前的地址拼上上传后的文件名字
     *      返回名字
     *
     * 上传返回什么，上传后文件名和对应在服务器的位置（得加ftp网址前缀）
     */

//    @RequestMapping(value = "upload.do")
//    @ResponseBody
//    public ServerResponse upload (HttpSession session,
//                                  @RequestParam(value = "upload_file", required = false) MultipartFile file,
//                                  HttpServletRequest request){
//        User user=(User)session.getAttribute(Const.CURRENT_USER);
//        if(user ==null){
//            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
//        }
//        if(iUserService.checkAdminRole(user).isSuccess()){
//
//
//
//
//
//
//        }
//        return ServerResponse.createByErrorMessage("无权限操作，请以管理员登陆");
//    }







}
