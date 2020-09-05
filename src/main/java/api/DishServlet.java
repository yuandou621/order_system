package api;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Dish;
import model.DishDao;
import model.User;
import util.OrderSystemException;
import util.OrderSystemUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/dish")
public class DishServlet extends HttpServlet {
    private Gson gson = new GsonBuilder().create();

    static class Request{
        public String name;
        public int price;
    }

    static class Response{
        public int ok;
        public  String reason;
    }
//新增菜品
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        //检查用户登陆状态
            try {
                HttpSession session = req.getSession(false);
                if(session == null) {
                    throw new OrderSystemException("当前未登录");
                }
                User user= (User) session.getAttribute("user");
                if(user == null){
                    throw new OrderSystemException("当前未登录");
                }
                //检查用户是否是管理员
                if(user.getIsAdmin() == 0){
                    throw new OrderSystemException("您不是管理员");
                }
                //读取请求body
                String body = OrderSystemUtil.readBody(req);
                //把body转换成Request对象
                Request request = gson.fromJson(body, Request.class);
                //构造Dish对象插入到数据库
                Dish dish = new Dish();
                dish.setName(request.name);
                dish.setPrice(request.price);
                DishDao dishDao = new DishDao();
                dishDao.add(dish);
                //结果返回客户端
                response.ok = 1;
                response.reason = "";
            } catch (OrderSystemException e) {
                response.ok = 0;
                response.reason = e.getMessage();
            }finally {
                resp.setContentType("application/json; charset=utf-8");
                String jsonString = gson.toJson(response);
                resp.getWriter().write(jsonString);
            }
    }
//删除菜品
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
            try {
                //检查用户是否登录
                HttpSession session = req.getSession(false);
                if(session == null) {
                    throw new OrderSystemException("您当前未登录");
                }
                User user = (User) session.getAttribute("user");
                if(user == null){
                    throw new OrderSystemException("您当前尚未登陆");
                }
                //检查用户是否是管理员
                if(user.getIsAdmin() == 0){
                    throw new OrderSystemException("您不是管理员");
                }
                //读取dishId
                String dishIdStr = req.getParameter("dishId");
                if(dishIdStr == null){
                    throw new OrderSystemException("dishId 不正确");
                }
                int dishId = Integer.parseInt(dishIdStr);
                //删除数据库中的对应菜品
                DishDao dishDao = new DishDao();
                dishDao.delete(dishId);
                //返回响应结果
                response.ok = 1;
                response.reason = "";
            } catch (OrderSystemException e) {
                response.ok = 0;
                response.reason = e.getMessage();
            }finally {
                resp.setContentType("application/json; charset=utf-8");
                String jsonString = gson.toJson(response);
                resp.getWriter().write(jsonString);
            }
    }
//查看所有菜品
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Response response = new Response();
        req.setCharacterEncoding("utf-8");
        resp.setContentType("application/json; charset=utf-8");
        //检测登陆状态
            try {
                HttpSession session = req.getSession(false);
                if (session == null) {
                    throw new OrderSystemException("当前尚未登陆");
                }
                User user =(User) session.getAttribute("user");
                if (user == null){
                    throw new OrderSystemException("当前尚未登陆");
                }
                //从数据库读取数据
                DishDao dishDao = new DishDao();
                List<Dish> dishes = dishDao.selectAll();
                //把结果返回页面
                String jsonStr = gson.toJson(dishes);
                resp.getWriter().write(jsonStr);
            } catch (OrderSystemException e) {
                response.ok = 0;
                response.reason = e.getMessage();
                String jsonStr = gson.toJson(response);
                resp.getWriter().write(jsonStr);
            }
    }
}
