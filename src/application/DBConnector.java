package application;

import java.sql.*;

public class DBConnector {
    //JDBC驱动名以及数据库URL
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    private static DBConnector instance = null;
    private Connection conn;
    private Connection handelconn;
    private Statement stm;
    private Statement handelstm;
    private DBConnector() throws ClassNotFoundException {
        //注册JDBC驱动
        Class.forName(JDBC_DRIVER);
    }
    //获取DBConnector的一个实例，保证该类只有一个实例
    public static DBConnector getInstance() {
        try {
            if (instance == null)
                instance = new DBConnector();
        } catch (ClassNotFoundException e) {
            System.out.println("Cannot load sql driver.");
            System.exit(1);
        }
        return instance;
    }

    /**
     * 连接数据库
     * @param host
     * @param port
     * @param dbName
     * @param user
     * @param pass
     * @throws SQLException
     */
    public void ConnectDataBase(String host, int port, String dbName,
                                String user, String pass) throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port +
                "/" + dbName +
                "?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=GMT";
        //连接数据库
        conn = DriverManager.getConnection(url, user, pass);
        stm = conn.createStatement();

        handelconn = DriverManager.getConnection(url, user, pass);
        handelconn.setAutoCommit(false);
        handelstm = handelconn.createStatement();
    }
    public void disConnectDataBase() throws SQLException {
        conn.close();
    }

    /**
     * 获取某一个表的所有列信息
     * @param tableName
     * @return 返回执行查询的结果集
     */
    public ResultSet getAllFromTable(String tableName) {
        try {
            System.out.println("Try get data from :" + tableName);
            return stm.executeQuery("select * from " + tableName);
        } catch (SQLException e) {
            System.out.println("The result from SQL is empty!");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据病人id查询病人信息
     * @param id 病人id
     * @return
     */
    public ResultSet getPatientInfo(String id) {
        try {
            return stm.executeQuery(
                    "select * from " + Config.NameTablePatient +
                            " where " + Config.NameTableColumnPatientNumber +
                            " = " + id
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据医生id编号查询医生信息
     * @param id    医生id
     * @return
     */
    public ResultSet getDoctorInfo(String id) {
        try {
            return stm.executeQuery(
                    "select * from " + Config.NameTableDoctor +
                            " where " + Config.NameTableColumnDoctorNumber +
                            " = " + id
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 挂号
     * @param registerCategoryNumber 号种编号
     * @param doctorNum 医生编号
     * @param patientNum    病人编号
     * @param registerFee   挂号费
     * @param deductFromBalance
     * @param addTobalance
     * @return
     */
    public int register(
            String registerCategoryNumber,
            String doctorNum,
            String patientNum,
            Double registerFee,
            boolean deductFromBalance,
            Double addTobalance
    ) throws RegisterExcption {
        try {
            //获取当前数据库中挂号编号的最大值
            ResultSet result = handelstm.executeQuery(
                    "select * from " + Config.NameTableRegister +
                            " order by " + Config.NameTableColumnRegisterNumber +
                            " desc limit 1"
            );

            int regNum, currCount;
            if(!result.next())
                regNum = 0;
            else
                regNum = Integer.parseInt(result.getString(Config.NameTableColumnRegisterNumber));
            //获取当前号种挂号统计
            result = handelstm.executeQuery(
                    "select * from " + Config.NameTableRegister +
                            " where " +Config.NameTableColumnRegisterCategoryNumber +
                            " = " + registerCategoryNumber + " order by " +
                            Config.NameTableColumnRegisterCurrentRegisterCount + " desc limit 1"
            );
            if (!result.next())
                currCount = 0;
            else
                currCount = result.getInt(Config.NameTableColumnRegisterCurrentRegisterCount);

            //查询该病人编号是否存在
            result = handelstm.executeQuery(
                    "select  * from " + Config.NameTablePatient +
                            " where " + Config.NameTableColumnPatientNumber +
                            " = " + patientNum
            );
            if(!result.next())
                throw new RegisterExcption("Patient not exist", RegisterExcption.ErrorCode.patientNotExist);
            //获取余额
            double balance = result.getDouble(Config.NameTableColumnPatientBalance);

            //从号种信息表中获取该号种编号的最大挂号限定人数
            result = handelstm.executeQuery(
                    "select " + Config.NameTableColumnCategoryRegisterMaxRegisterNumber +
                            " from " + Config.NameTableCategoryRegister +
                            " where " + Config.NameTableColumnCategoryRegisterNumber +
                            " = " + registerCategoryNumber
            );

            int maxRegCount;
            if (!result.next())
                throw new RegisterExcption("号种编号不合法", RegisterExcption.ErrorCode.registerCategoryNotFound);
            maxRegCount = result.getInt(Config.NameTableColumnCategoryRegisterMaxRegisterNumber);
            //
            if (currCount >= maxRegCount) {
                throw new RegisterExcption("该号种已达到最大挂号限定", RegisterExcption.ErrorCode.registerNumberExceeded);
            }

            //向挂号信息表中插入一个新的表项
            handelstm.executeUpdate(
                    String.format("insert into %s values (\"%06d\",\"%s\",\"%s\",\"%s\",%d,false,%s,current_timestamp",
                            Config.NameTableRegister,
                            regNum, registerCategoryNumber, doctorNum, patientNum, currCount + 1, registerFee)
            );
            //从余额扣费
            if(deductFromBalance) {
                if (balance >= registerFee) {
                    //当前余额足够缴费,则更新病人余额
                    handelstm.executeUpdate(
                            String.format("update %s set %s=%.2f where %s=%s",
                                    Config.NameTablePatient,
                                    Config.NameTableColumnPatientBalance,
                                    (balance -= registerFee),
                                    Config.NameTableColumnPatientNumber,
                                    patientNum
                            )
                    );
                }
                else {
                    System.out.println("余额不足");
                }
            }

            //将找零金额转入余额中
            if(addTobalance != 0) {
                handelstm.executeUpdate(
                        String.format("update %s set %s=%.2f where %s=%s",
                                Config.NameTablePatient,
                                Config.NameTableColumnPatientBalance,
                                (balance += addTobalance),
                                Config.NameTableColumnPatientNumber,
                                patientNum)
                );
            }

            handelconn.commit();
            return regNum;

        } catch (SQLException e) {
            try {
                handelconn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
                throw new RegisterExcption("数据库发生错误", RegisterExcption.ErrorCode.sqlException);
            }
        }
        return 0;
    }

    /**
     * 医生获取挂号信息
     * @param docNum
     * @param startTime
     * @param endTime
     * @return
     */
    public ResultSet getRegisterForDoctor(String docNum, String startTime, String endTime) {
        String sql = "select reg." + Config.NameTableColumnRegisterNumber +
                ", pat." + Config.NameTableColumnPatientNumber +
                ", reg." + Config.NameTableColumnRegisterDateTime +
                ", cat." + Config.NameTableColumnCategoryRegisterIsSpecialist + (
                        " from (select " + Config.NameTableColumnRegisterNumber +
                                "," + Config.NameTableColumnRegisterPatientNumber +
                                "," + Config.NameTableColumnRegisterDateTime +
                                "," + Config.NameTableColumnRegisterCategoryNumber +
                                " from " + Config.NameTableRegister +
                                " where " + Config.NameTableColumnRegisterDoctorNumber +
                                " = " + docNum +
                                " and " + Config.NameTableColumnRegisterDateTime +
                                " >= \"" + startTime + "\" and " + Config.NameTableColumnRegisterDateTime +
                                " <= \"" + endTime + "\") as reg") + (
                        " inner join (select " + Config.NameTableColumnPatientNumber +
                        "," + Config.NameTableColumnPatientName + " from " +
                                Config.NameTablePatient + ") as pat" ) +
                " on reg." + Config.NameTableColumnRegisterPatientNumber +
                " = pat." + Config.NameTableColumnPatientNumber + (
                        " inner join (select " + Config.NameTableColumnCategoryRegisterNumber +
                                "," + Config.NameTableColumnCategoryRegisterIsSpecialist +
                                " from " + Config.NameTableCategoryRegister + ") as cat") +
                " on reg." + Config.NameTableColumnRegisterCategoryNumber +
                " = cat." + Config.NameTableColumnCategoryRegisterNumber;
        try {
            return stm.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet getIncomeInfo(String startTime, String endTime) {
        String sql = "select dep." + Config.NameTableColumnDepartmentName +
                "as depname, reg." + Config.NameTableColumnRegisterDoctorNumber +
                ",doc." + Config.NameTableColumnDepartmentName +
                " as docname, cat." + Config.NameTableColumnCategoryRegisterIsSpecialist +
                ", reg." + Config.NameTableColumnRegisterCurrentRegisterCount +
                ", SUM(reg." + Config.NameTableColumnRegisterFee +
                ") as sum from " + (
                        " (select * from " + Config.NameTableRegister +
                                " where " + Config.NameTableColumnRegisterDateTime +
                                ">=\"" + startTime + "\" and " +
                                Config.NameTableColumnRegisterDateTime + "<=\"" + endTime +
                                "\") as reg"
                ) + " inner join " + (
                        " (select " + Config.NameTableColumnDoctorNumber +
                                "," + Config.NameTableColumnDoctorName +
                                "," + Config.NameTableColumnDoctorDepartmentNumber +
                                " from " + Config.NameTableDoctor +
                                ") as doc"
                ) + " on reg." + Config.NameTableColumnRegisterDoctorNumber +
                " = doc." + Config.NameTableColumnDoctorNumber +
                " inner join "+ (
                        " (select " + Config.NameTableColumnDepartmentNumber +
                                "," + Config.NameTableColumnDepartmentName +
                                " from " + Config.NameTableDepartment + ") as dep"
                ) + "on doc." + Config.NameTableColumnDoctorDepartmentNumber +
                " = dep." + Config.NameTableColumnDepartmentNumber +
                " inner join " + (
                        " (select " + Config.NameTableColumnCategoryRegisterNumber +
                                "," + Config.NameTableColumnCategoryRegisterIsSpecialist +
                                " from " + Config.NameTableCategoryRegister + ") as cat"
                ) + " on reg." + Config.NameTableColumnRegisterCategoryNumber +
                " = cat." + Config.NameTableColumnCategoryRegisterNumber +
                " group by reg." + Config.NameTableColumnRegisterDoctorNumber +
                ", cat." + Config.NameTableColumnCategoryRegisterIsSpecialist;
        try {
            return stm.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updatePatientLoginTime(String pid, String time) {
        try {
            stm.executeUpdate(
                    "update " + Config.NameTablePatient +
                            " set " + Config.NameTableColumnPatientLastLogin +
                            " = \"" + time + "\" where " + Config.NameTableColumnPatientNumber +
                            " = " + pid
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return ;
        }
    }
    public void updateDoctorLoginTime(String doct_id, String time) {
        try {
            stm.executeUpdate(
                    "update " + Config.NameTableDoctor +
                            " set " + Config.NameTableColumnDoctorLastLogin +
                            " = \"" + time + "\" where " + Config.NameTableColumnDoctorNumber +
                            " = " + doct_id
            );
        } catch (SQLException e) {
            e.printStackTrace();
            return ;
        }
    }
}

class RegisterExcption extends Exception {
    public enum ErrorCode {
        noError,
        registerCategoryNotFound,
        registerNumberExceeded,
        patientNotExist,
        sqlException,
        retryTimeExceeded
    }
    ErrorCode error;
    RegisterExcption(String reason, ErrorCode error) {
        super(reason);
        this.error = error;
    }
}