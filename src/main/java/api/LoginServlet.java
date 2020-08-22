package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import model.User;
import model.UserDao;
import util.OrderSystemException;
import util.OrderSystemUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

@WebServlet("/Login")
public class LoginServlet extends HttpServlet {
    private Gson gson = new GsonBuilder().create();

    static class Request{
        public String name;
        public String password;
    }

    static class Response{
        public int ok;
        public String reason;
        public String name;
        public int isAdmin;
    }

    //登录功能api
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        try {
            //读取body数据
            String body = OrderSystemUtil.readBody(req);
            //将读取的数据解析成对象
            Request request = gson.fromJson(body, Request.class);
            //按照用户名进行查找，校验密码
            UserDao userDao = new UserDao();
            User user =  userDao.selectByName(request.name);
            if(user == null || !user.getPassword().equals(request.password)){
                throw new OrderSystemException("用户名或密码错误");
            }
            //如果登陆成功，创建session对象
            HttpSession session = req.getSession(true);
            session.setAttribute("user",user);
            response.reason = "";
            response.name = user.getName();
            response.isAdmin = user.getIsAdmin();
        } catch (OrderSystemException e) {
            //如果登陆失败，返回错误结果
            response.ok = 0;
            response.reason = e.getMessage();
        }finally {
            //结果写回客户端
            resp.setContentType("application/json; charset=utf-8");
            String jsonString = gson.toJson(response);
            resp.getWriter().write(jsonString);
        }
    }

    //检测登陆状态api
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
            try {
                //获取用户当前session,不存在认为未登录
                HttpSession session = req.getSession(false);
                if (session == null) {
                    throw new OrderSystemException("当前未登录");
                }
                //从session中获取user对象
                User user =(User) session.getAttribute("user");
                if(user == null) {
                    throw new OrderSystemException("当前未登录");
                }
                //把user中信息填进返回值结果中
                response.ok = 1;
                response.reason = "";
                response.name = user.getName();
                response.isAdmin = user.getIsAdmin();
            }catch (OrderSystemException e) {
                response.ok = 0;
                response.reason = e.getMessage();
            }finally {
                //结果写回客户端
                resp.setContentType("application/json; charset=utf-8");
                String jsonString = gson.toJson(response);
                resp.getWriter().write(jsonString);
        }
    }
}
