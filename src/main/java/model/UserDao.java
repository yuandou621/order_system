package model;


import util.OderSystemException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    public void add(User user) throws OderSystemException {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("insert into user values(null,?,?,?)");
            statement.setString(1, user.getName());
            statement.setString(2, user.getPassword());
            statement.setInt(3, user.getIsAdmin());

            int ret = statement.executeUpdate();
            if (ret != 1) {
                throw new OderSystemException("插入用户失败");
            }
            System.out.println("插入用户成功");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OderSystemException("插入用户失败");
        } finally {
            DBUtil.Close(connection, statement, null);
        }
    }

    public User selectByName(String name) throws OderSystemException {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("select * from user where name = ?");
            statement.setString(1, name);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt("userId"));
                user.setName(resultSet.getString("name"));
                user.setPassword(resultSet.getString("password"));
                user.setIsAdmin(resultSet.getInt("isAdmin"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OderSystemException("按姓名查找用户失败");
        } finally {
            DBUtil.Close(connection, statement, resultSet);
        }
        return null;
    }

    public User selectById(int userId) throws OderSystemException {
        Connection connection = DBUtil.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("select * from user where userId = ?");
            statement.setInt(1, userId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt("userId"));
                user.setName(resultSet.getString("name"));
                user.setPassword(resultSet.getString("password"));
                user.setIsAdmin(resultSet.getInt("isAdmin"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OderSystemException("按id查找用户失败");
        } finally {
            DBUtil.Close(connection,statement,resultSet);
        }
        return null;
    }

    public static void main(String[] args) throws OderSystemException {
        UserDao userDao = new UserDao();

        User user = new User();
        user.setName("小宝");
        user.setPassword("123456");
        user.setIsAdmin(0);
        userDao.add(user);
    }
}
