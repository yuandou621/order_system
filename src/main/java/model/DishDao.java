package model;

import util.OderSystemException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//1、新增菜品
//2、删除菜品
//3、查询所有菜品
//5、查询指定菜品
public class DishDao {
    public void add(Dish dish) throws OderSystemException {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("insert into dishes values(null,?,?)");
            statement.setString(1,dish.getName());
            statement.setInt(2,dish.getPrice());
            int ret = statement.executeUpdate();
            if(ret != 1){
                throw new OderSystemException("插入菜品失败");
            }
            System.out.println("插入菜品成功");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OderSystemException("插入菜品失败");
        }finally {
            DBUtil.Close(connection,statement,null);
        }
    }

    public  void delete(int dishId) throws OderSystemException {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("delete from dishes where dishId = ?");
            statement.setInt(1,dishId);
            int ret = statement.executeUpdate();
            if( ret != 1){
                throw new OderSystemException("删除菜品失败");
            }
            System.out.println("删除菜品成功");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OderSystemException("删除菜品失败");
        }finally{
            DBUtil.Close(connection,statement,null);
        }
    }

    public List<Dish> selectAll() throws OderSystemException {
        List<Dish> dishes = new ArrayList<>();
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("select * from dishes");
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Dish dish = new Dish();
                dish.setDishId(resultSet.getInt("dishId"));
                dish.setName(resultSet.getString("name"));
                dish.setPrice(resultSet.getInt("price"));
                dishes.add(dish);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OderSystemException("查找所有菜品失败");
        } finally {
            DBUtil.Close(connection,statement,resultSet);
        }
        return dishes;
    }

    public Dish selectById(int dishId) throws OderSystemException {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("select * from dishes where dishId = ?");
            statement.setInt(1, dishId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Dish dish = new Dish();
                dish.setDishId(resultSet.getInt("dishId"));
                dish.setName(resultSet.getString("name"));
                dish.setPrice(resultSet.getInt("price"));
                return dish;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OderSystemException("按id查找菜品失败");
        } finally {
            DBUtil.Close(connection, statement, resultSet);
        }
        return null;
    }
}
