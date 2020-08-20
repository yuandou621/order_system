package api;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.User;
import model.UserDao;
import util.OrderSystemException;
import util.OrderSystemUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private Gson gson = new GsonBuilder().create();

    //读取JSON请求对象
    static class Request{
        public String name;
        public String password;
    }
    //构造JSON响应对象
    static class Response{
        public int ok;
        public String reason;
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Response response = new Response();
        try {
            //读取body中的数据
            String body = OrderSystemUtil.readBody(req);
            //解析body中的数据成Request对象
            Request request = gson.fromJson(body, Request.class);
            //查询数据库，当前用户名是否存在
            UserDao userDao = new UserDao();
            User existUser =  userDao.selectByName(request.name);
            if(existUser != null){
                //当前用户名重复
                throw new OrderSystemException("当前用户名已存在");
            }
            //把提交的用户名密码构造成User对象，提交给数据库
            User user = new User();
            user.setName(request.name);
            user.setPassword(request.password);
            user.setIsAdmin(0);

            userDao.add(user);
        } catch (OrderSystemException e){
            response.ok = 0;
            response.reason = e.getMessage();
        } finally {
            //构造响应数据
            String jsonString = gson.toJson(response);
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().write(jsonString);
        }
    }
}
