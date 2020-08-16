package model;

import util.OrderSystemException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {
    //新增订单
    public void add(Order order) throws OrderSystemException {
        //先插入order_user表
        addOrderUser(order);
        //插入order_dish表
        addOrderDish(order);

    }
    private void addOrderUser(Order order) throws OrderSystemException {
        //建立数据库连接
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        String sql = "insert into order_user values(null, ?, now(), 0)";
        ResultSet resultSet = null;
        try {
            //插入的同时获取自增主键
            statement = connection.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setInt(1,order.getUserId());
            int ret = statement.executeUpdate();
            if(ret != 1){
                throw new OrderSystemException("插入订单失败");
            }
            //获取自增主键的值
            resultSet = statement.getGeneratedKeys();
            if(resultSet.next()){
                order.setOrderId(resultSet.getInt(1));
            }
            System.out.println("order_user插入订单成功");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("插入订单失败");
        }finally {
            DBUtil.Close(connection,statement,resultSet);
        }
    }
    private void addOrderDish(Order order) {
        //建立数据库连接
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        try {
            //关闭自动提交
            connection.setAutoCommit(false);
            statement = connection.prepareStatement("insert into order_dish values (?, ?)");
            //一个订单对应多个菜品，遍历Order中的菜品
            List<Dish> dishes = order.getDishes();
            for (Dish dish : dishes) {
                statement.setInt(1,order.getOrderId());
                statement.setInt(2, dish.getDishId());
                //给sql新增一个片段
                statement.addBatch();
            }
            //执行sql语句
            statement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            //order_dish删除失败，整体插入失败，回滚之前插入的order_user表

        }finally {
            DBUtil.Close(connection,statement,null);
        }
    }
    private  void deleteOrderUser(int orderId) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("delete from order_user where orderId = ?");
            statement.setInt(1,orderId);
            int ret = statement.executeUpdate();
            if( ret != 1){
                throw new OrderSystemException("回滚失败");
            }
            System.out.println("回滚成功");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("回滚失败");
        }finally{
            DBUtil.Close(connection,statement,null);
        }
    }
    //获取所有订单
    public List<Order> selectAll(){
        List<Order> orders = new ArrayList<>();
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("select * from order_user ");
            resultSet = statement.executeQuery();
            while(resultSet.next()){
                Order order = new Order();
                order.setOrderId(resultSet.getInt("orderId"));
                order.setUserId(resultSet.getInt("userId"));
                order.setTime(resultSet.getTimestamp( "time"));
                order.setIsDone(resultSet.getInt("isDone"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtil.Close(connection,statement,resultSet);
        }
        return orders;
    }

    public  List<Order> selectByUserId(int userId){
        List<Order> orders = new ArrayList<>();
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("select * from order_user where userId = ?");
            statement.setInt(1,userId);
            resultSet = statement.executeQuery();
            while(resultSet.next()){
                Order order = new Order();
                order.setOrderId(resultSet.getInt("orderId"));
                order.setUserId(resultSet.getInt("userId"));
                order.setTime(resultSet.getTimestamp( "time"));
                order.setIsDone(resultSet.getInt("isDone"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtil.Close(connection,statement,resultSet);
        }
        return orders;
    }

    public  Order selectById(int orderId) throws OrderSystemException {
        Order order = buildOrder(orderId);

        List<Integer> dishIds = selectDishIds(orderId);

        order = getDishDetall(order,dishIds);
        return order;
    }

    private Order buildOrder(int orderId) {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet =null;
        try {
            statement = connection.prepareStatement("select * from order_user where orderId = ?");
            statement.setInt(1,orderId);
            resultSet = statement.executeQuery();
            while (resultSet.next()){
                Order order = new Order();
                order.setOrderId(resultSet.getInt("orderId"));
                order.setUserId(resultSet.getInt("userId"));
                order.setTime(resultSet.getTimestamp( "time"));
                order.setIsDone(resultSet.getInt("isDone"));
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtil.Close(connection,statement,resultSet);
        }
        return null;
    }

    private List<Integer> selectDishIds(int orderId) {
        List<Integer> dishIds = new ArrayList<>();
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet =null;
        try {
            statement = connection.prepareStatement("select * from order_dish where orderId = ?");
            statement.setInt(1,orderId);
            resultSet = statement.executeQuery();
            while (resultSet.next()){
                dishIds.add(resultSet.getInt("dishId"));
                return dishIds;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtil.Close(connection,statement,resultSet);
        }
        return dishIds;
    }

    private Order getDishDetall(Order order, List<Integer> dishIds) throws OrderSystemException {
        List<Dish> dishes = new ArrayList<>();
        DishDao dishDao = new DishDao();
        for(Integer dishId : dishIds){
            Dish dish = dishDao.selectById(dishId);
            dishes.add(dish);
        }
        order.setDishes(dishes);
        return order;
    }

    public void changeState(int orderId, int isDone) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statment = null;
        try {
            statment = connection.prepareStatement("update order_user set isDone = ? where orderId = ?");
            statment.setInt(1,isDone);
            statment.setInt(2,orderId);
            int ret = statment.executeUpdate();
            if(ret != 1){
                throw new OrderSystemException("修改订单状态失败");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtil.Close(connection,statment,null);
        }
    }
}
