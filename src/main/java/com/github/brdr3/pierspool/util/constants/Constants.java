package com.github.brdr3.pierspool.util.constants;

import com.github.brdr3.pierspool.util.User;

public class Constants {
    public static final String address[] = {"localhost", "localhost", "localhost", "localhost", "localhost"};
    public static final int port[] = {13000, 13010, 13020, 13030, 13040};
    
    
    public static final User users[] = {new User(0, address[0], port[0]), 
                                        new User(1, address[1], port[1]), 
                                        new User(2, address[2], port[2]), 
                                        new User(3, address[3], port[3]), 
                                        new User(4, address[4], port[4])};
}
