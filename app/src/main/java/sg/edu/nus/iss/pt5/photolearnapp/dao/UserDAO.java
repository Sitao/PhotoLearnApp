package sg.edu.nus.iss.pt5.photolearnapp.dao;

import sg.edu.nus.iss.pt5.photolearnapp.model.User;

/**
 * Created by Liang Entao on 20/3/18.
 */
public class UserDAO extends BaseEntityDAO<User> {
    public UserDAO() {
        super("users", User.class);
    }
}
