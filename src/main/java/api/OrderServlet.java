package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Dish;
import model.Order;
import model.OrderDao;
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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/order")
public class OrderServlet extends HttpServlet {
    private Gson gson = new GsonBuilder().create();

    static  class Response{
        public int ok;
        public  String reason;
    }
    //新增订单（管理员没有权限新增订单）
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Response response = new Response();
        req.setCharacterEncoding("utf-8");
        //检查用户是否登录
        try {
            HttpSession session = req.getSession(false);
            if(session == null){
                throw new OrderSystemException("当前未登录");
            }
            User user =(User) session.getAttribute("user");
            if(user == null){
                throw new OrderSystemException("当前尚未登陆");
            }
            //判断是否是管理员
            if(user.getIsAdmin() == 1){
                throw new OrderSystemException("您是管理员无权下单");
            }
            //读取body中的数据并解析
            String body = OrderSystemUtil.readBody(req);
            Integer[] dishIds = gson.fromJson(body,Integer[].class);
            //构造order对象
            Order order = new Order();
            order.setUserId(user.getUserId());
            List<Dish> dishes = new ArrayList<>();
            for(Integer dishId : dishIds){
                Dish dish = new Dish();
                dish.setDishId(dishId);
                dishes.add(dish);
            }
            order.setDishes(dishes);
            //插入数据库
            OrderDao orderDao = new OrderDao();
            orderDao.add(order);
            response.ok = 1;
            response.reason = "";
        }catch (OrderSystemException e){
            response.ok = 0;
            response.reason = e.getMessage();
        }finally {
            resp.setContentType("application/json; charset=utf-8");
            String jsonStr = gson.toJson(response);
            resp.getWriter().write(jsonStr);
        }
    }
    //查看订单

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        resp.setContentType("application/json; charset=utf-8");
        Response response = new Response();
        List<Order> orders = new ArrayList<>();
        try {
            HttpSession session = req.getSession(false);
            if(session == null) {
                throw new OrderSystemException("当前未登录");
            }
            User user= (User) session.getAttribute("user");
            if(user == null){
                throw new OrderSystemException("当前未登录");
            }
            //判断用户是否是管理员
            //读取orderId字段，判断字段是否存在
            OrderDao orderDao = new OrderDao();
            String orderIdStr = req.getParameter("orderId");
            if(orderIdStr == null){
                //普通用户，只查看自己的订单
                if(user.getIsAdmin() == 0){
                    orders = orderDao.selectByUserId(user.getUserId());
                }else{
                    //管理员查看所有订单
                    orders = orderDao.selectAll();
                }
                //构造响应结果
                String jsonStr = gson.toJson(orders);
                resp.getWriter().write(jsonStr);
            }else{
                int orderId = Integer.parseInt(orderIdStr);
                Order order = orderDao.selectById(orderId);
                if(order.getUserId() != user.getUserId() && user.getIsAdmin() == 0 ){
                    throw new OrderSystemException("您当前没有权限查看他人订单");
                }
                //构造响应结果
                resp.setContentType("application/json; charset=utf-8");
                String jsonStr = gson.toJson(order);
                resp.getWriter().write(jsonStr);
            }
        }catch (OrderSystemException e){
            response.ok = 0;
            response.reason = e.getMessage();
            String jsonStr = gson.toJson(response);
            resp.getWriter().write(jsonStr);
        }
    }
//修改订单状态
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        try {
            //检查用户登陆状态
            HttpSession session = req.getSession(false);
            if(session == null){
                throw new OrderSystemException("当前尚未登陆");
            }
            User user =(User) session.getAttribute("user");
            if(user == null){
                throw new OrderSystemException("当前尚未登陆");
            }
            //判断是否是管理员
            if(user.getIsAdmin() == 0){
                throw new OrderSystemException("您不是管理员");
            }
            //读取请求中的字段
            String orderIdStr = req.getParameter("orderId");
            String isDoneStr = req.getParameter("isDone");
            if(orderIdStr == null || isDoneStr == null){
                throw new OrderSystemException("当前输入参数有误");
            }
            //修改数据库
            OrderDao orderDao = new OrderDao();
            int orderId = Integer.parseInt(orderIdStr);
            int isDone = Integer.parseInt(isDoneStr);
            orderDao.changeState(orderId, isDone);
            //返回响应结果
            response.ok = 1;
            response.reason = "";
        }catch (OrderSystemException e){
            response.ok = 0;
            response.reason = e.getMessage();
        }finally {
            resp.setContentType("application/json; charset=utf-8");
            String jsonStr = gson.toJson(response);
            resp.getWriter().write(jsonStr);
        }
    }
}
