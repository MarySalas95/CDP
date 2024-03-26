package com.comp.complementos.DB;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbcp.BasicDataSource;

/**
 *
 * @author msalas
 */
public class Pool {

    private static BasicDataSource ds = null;

    public static DataSource getDataSource() {

        if (ds == null) {
            ds = new BasicDataSource();
            ds.setDriverClassName("com.ibm.as400.access.AS400JDBCDriver");
            
            //Ambiente Demo
            ds.setUsername("");
            ds.setPassword("");
            ds.setUrl("");
            
            //Ambiente Real
            /*ds.setUsername("");
            ds.setPassword("");
            ds.setUrl("");*/
            
            ds.setInitialSize(15);//Número de conexiones que se quiere que se abran en cuanto el Pool comienza a trabajar
            ds.setMaxIdle(5); //Número máximo de conexiones inactivas que queremos que haya 
            ds.setMaxWait(600);
            //ds.setPoolPreparedStatements(true);
            System.out.println("Pool de Conexiones Creado!");
        }
        return ds;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

}
