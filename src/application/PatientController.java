package application;

import com.sun.javafx.robot.impl.FXRobotHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * 下拉列表选项通用类
 */
abstract class ListItem {
    public String pronounce;

    @Override
    public abstract String toString();

    public abstract void fromSqlResult(ResultSet result) throws SQLException;
    public String getPronounce() {
        return pronounce;
    }
}

/**
 * 科室名称下拉列表选项
 */
class ListItemForDepName extends ListItem {
    public String number;
    public String name;

    @Override
    public String toString() {
        return number + " " + name + " ";
    }

    @Override
    public void fromSqlResult(ResultSet result) throws SQLException {
        number = result.getString(Config.NameTableColumnDepartmentNumber);
        name = result.getString(Config.NameTableColumnDepartmentName);
        pronounce = result.getString(Config.NameTableColumnDepartmentPronounce);
    }
}

/**
 * 医生名称下拉列表选项
 */
class ListItemForDoctName extends ListItem {
    public String name;
    public String number;
    public String deptNum;
    public boolean isSpecialist;

    @Override
    public String toString() {
        return number + " " + name + " " + (isSpecialist ? "专家号" : "普通号");
    }

    @Override
    public void fromSqlResult(ResultSet result) throws SQLException {
        name = result.getString(Config.NameTableColumnDoctorName);
        number = result.getString(Config.NameTableColumnDoctorNumber);
        deptNum = result.getString(Config.NameTableColumnDoctorDepartmentNumber);
        isSpecialist = result.getBoolean(Config.NameTableColumnDoctorIsSpecialist);
        pronounce = result.getString(Config.NameTableColumnDoctorPronounce);
    }
}

/**
 * 号种名称下拉列表选项
 */
class ListItemForRegName extends ListItem {
    public String name;
    public String number;
    public double fee;
    public String department;
    public int maxNum;
    public boolean isSpecialist;
    @Override
    public String toString() {
        return number + " " + name + " " + (isSpecialist? "专家号" : "普通号") + fee + "￥";
    }

    @Override
    public void fromSqlResult(ResultSet result) throws SQLException {
        name = result.getString(Config.NameTableColumnCategoryRegisterName);
        number = result.getString(Config.NameTableColumnCategoryRegisterNumber);
        fee = result.getDouble(Config.NameTableColumnCategoryRegisterFee);
        isSpecialist = result.getBoolean(Config.NameTableColumnCategoryRegisterIsSpecialist);
        department = result.getString(Config.NameTableColumnCategoryRegisterDepartment);
        maxNum = result.getInt(Config.NameTableColumnCategoryRegisterMaxRegisterNumber);
        pronounce = result.getString(Config.NameTableColumnCategoryRegisterPronounce);
    }
}

/**
 * 号种类别下拉菜单选项
 */
class ListItemForRegType extends ListItem {
    public boolean isSpecialist;
    @Override
    public String toString() {
        return isSpecialist? "专家号" : "普通号";
    }

    @Override
    public void fromSqlResult(ResultSet result) throws SQLException {

    }
}
public class PatientController implements Initializable {

    //Patient的这几个属性由logincontroller初始化
    public static String PatientName, PatientNumber;
    public static double PatientBalance;
    @FXML
    private Button button_enter;
    @FXML
    private Button button_clear;
    @FXML
    private Button button_exit;
    @FXML
    private ComboBox
            inputdep,
            inputdoctname,
            inputtyperegister,
            inputnamecat;
    @FXML
    private TextField inputpay;
    @FXML
    private Label labelfee, labelrefund, labelregnum, labelinfo,labelStatus;
    @FXML
    private CheckBox usebalance, addTobalance;
    @FXML
    private SplitPane mainPane;

    //下拉列表选项
    private ObservableList<ListItemForDepName> listDeptName = FXCollections.observableArrayList();
    private ObservableList<ListItemForDoctName> listDoctName = FXCollections.observableArrayList();
    private ObservableList<ListItemForRegType> listRegtype = FXCollections.observableArrayList();
    private ObservableList<ListItemForRegName> listRegName = FXCollections.observableArrayList();

    //根据输入过滤后的下拉列表选项
    private ObservableList<ListItemForDepName> listDeptNameFiltered = FXCollections.observableArrayList();
    private ObservableList<ListItemForDoctName> listDoctNameFiltered = FXCollections.observableArrayList();
    private ObservableList<ListItemForRegType> listRegtypeFiltered = FXCollections.observableArrayList();
    private ObservableList<ListItemForRegName> listRegNameFiltered = FXCollections.observableArrayList();

    private int lastIndexInputNameDepartment = -1;
    private int lastIndexInputNameDoctor = -1;
    private int lastIndexInputTypeRegister = -1;
    private int lastIndexInputNameRegister = -1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //初始化函数
        updateDisplayInfo();
        FXRobotHelper.getStages().get(0).setTitle("挂号");

        //初始化数据
        updateData();

        //初始化下拉列表选项
        inputdep.setItems(FXCollections.observableArrayList());
        inputdoctname.setItems(FXCollections.observableArrayList());
        inputtyperegister.setItems(FXCollections.observableArrayList());
        inputnamecat.setItems(FXCollections.observableArrayList());

        reFilterDepartment(false);
        reFilterDoctor(false);
        reFilterRegisterType(false);
        reFilterRegisterName(false);

        //更新确定按钮的状态
        updateButtonEnter();

        //filter content by key typed
        inputdep.getEditor().setOnKeyReleased(KeyEvent -> {
            if(shouldPassKeyCode(KeyEvent.getCode()))
                return;
            //根据输入的关键字进行检索
            reFilterDepartment(true);
            reFilterDoctor(false);
            reFilterRegisterType(false);
            reFilterRegisterName(false);
            if (!inputdep.isShowing())
                inputdep.show();
            else {
                inputdep.hide();
                inputdep.show();
            }
        });
        inputdep.addEventHandler(ComboBox.ON_HIDDEN, e -> {
            int index;
            if((index = inputdep.getSelectionModel().getSelectedIndex())
            != lastIndexInputNameDepartment) {
                lastIndexInputNameDepartment = index;
                reFilterDoctor(false);
                reFilterRegisterType(false);
                reFilterRegisterName(false);
            }
            e.consume();
        });


        inputdoctname.getEditor().setOnKeyReleased(KeyEvent -> {
            if(shouldPassKeyCode(KeyEvent.getCode()))
                return;

            reFilterDoctor(true);
            reFilterDepartment(false);
            reFilterRegisterType(false);
            reFilterRegisterName(false);
            if (!inputdoctname.isShowing())
                inputdoctname.show();
            else {
                inputdoctname.hide();
                inputdoctname.show();
            }
        });
        inputdoctname.addEventHandler(ComboBox.ON_HIDDEN,e -> {
            int index;
            if((index = inputdoctname.getSelectionModel().getSelectedIndex())
            != lastIndexInputNameDoctor) {
                lastIndexInputNameDoctor = index;
                reFilterDepartment(false);
                reFilterRegisterType(false);
                reFilterRegisterName(false);
            }
            inputdoctname.setStyle("");
            updateButtonEnter();
            e.consume();
        });
        inputdoctname.setOnMouseClicked(MouseEvent -> {
            inputdep.setStyle("");
        });


        inputtyperegister.getEditor().setOnKeyReleased(KeyEvent -> {
            if(shouldPassKeyCode(KeyEvent.getCode()))
                return;
            System.out.println("Begin");
            reFilterRegisterType(true);
            System.out.println("index:" + inputdoctname.getSelectionModel().getSelectedIndex());
            reFilterDepartment(false);
            System.out.println("index:" + inputdoctname.getSelectionModel().getSelectedIndex());
            reFilterDoctor(false);
            System.out.println("index:" + inputdoctname.getSelectionModel().getSelectedIndex());
            reFilterRegisterName(false);
            System.out.println("index:" + inputdoctname.getSelectionModel().getSelectedIndex());
            System.out.println("End");
            if(!inputtyperegister.isShowing())
                inputtyperegister.show();
            else {
                inputtyperegister.hide();
                inputtyperegister.show();
            }
        });
        inputtyperegister.addEventHandler(ComboBox.ON_HIDDEN, e -> {
            int index;
            if((index = inputtyperegister.getSelectionModel().getSelectedIndex()) !=
            lastIndexInputTypeRegister) {
                lastIndexInputTypeRegister = index;
                reFilterDepartment(false);
                reFilterDoctor(false);
                reFilterRegisterName(false);
            }
            updateButtonEnter();
            e.consume();
        });
        inputnamecat.getEditor().setOnKeyReleased(KeyEvent -> {
            if(shouldPassKeyCode(KeyEvent.getCode()))
                return;

            reFilterRegisterName(true);
            reFilterDepartment(false);
            reFilterDoctor(false);
            reFilterRegisterType(false);


            if(!inputnamecat.isShowing())
                inputnamecat.show();
            else {
                inputnamecat.hide();
                inputnamecat.show();
            }
        });

        inputnamecat.addEventHandler(ComboBox.ON_HIDDEN, e -> {
            int index;
            if((index = inputnamecat.getSelectionModel().getSelectedIndex()) !=
            lastIndexInputNameRegister) {
                lastIndexInputNameRegister = index;
                reFilterDepartment(false);
                reFilterDoctor(false);
                reFilterRegisterType(false);
            }
            inputnamecat.setStyle("");
            if(index!=-1) {
                double fee = listRegNameFiltered.get(index).fee;
                labelfee.setText(fee + " ￥");
            }
            updateButtonEnter();
            updateRefund();
            updateUseBalance();
            e.consume();
        });
        inputnamecat.setOnMouseClicked(MouseEvent -> {
            inputnamecat.setStyle("");
        });

        usebalance.setOnKeyReleased(KeyEvent -> {
            if (KeyEvent.getCode() == KeyCode.ENTER)
                useBalanceSelected();
            else
                KeyEvent.consume();
        });
        usebalance.setOnMouseClicked(MouseEvent -> {
            useBalanceSelected();
            System.out.println("MouseClicked!");
        });

        inputpay.setOnKeyReleased(KeyEvent -> {
            if (KeyEvent.getCode() == KeyCode.ENTER) {
                updateRefund();
                updateButtonEnter();
            }
        });
    }
    //更新病人基本信息显示
    public void updateDisplayInfo() {
        labelinfo.setText(String.format("欢迎, %s!     余额：%.2f￥", PatientName, PatientBalance));
    }

    /**
     * 更新下拉列表选项
     * @param tableName 数据库中的表名称
     * @param list  下拉列表
     * @param itemTypeClass 获取元素的类型
     * @param <ItemType>    下拉列表中每个元素的类型
     * @return
     */
    private <ItemType extends ListItem> boolean updateOneComboBoxOfData (
            String tableName, ObservableList<ItemType> list, Class<ItemType> itemTypeClass) {
        //获取整张表的数据
        ResultSet result = DBConnector.getInstance().getAllFromTable(tableName);

        if (result != null) {
            System.out.println("Get content from SQL");
            ObservableList<ItemType> tmplist = FXCollections.observableArrayList();

            //将表中的所有组的有关信息添加到下拉列表选项中
            try {
                while (result.next()) {
                    ItemType item = itemTypeClass.newInstance();
                    item.fromSqlResult(result);
                    //add to table
                    tmplist.add(item);
//                    System.out.println(item);
                }
            } catch (SQLException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                System.exit(-1);
            }

            //上述操作都没有出错则将结果拷贝到下拉列表中
            list.clear();
            list.addAll(tmplist);
        }
        else {
            System.out.println("Empty Set");
        }
        return true;
    }

    public void updateData() {
        //更新科室名称下拉列表
        updateOneComboBoxOfData(
                Config.NameTableDepartment,
        listDeptName,  ListItemForDepName.class);
        //更新医生姓名下拉列表
        updateOneComboBoxOfData(
                Config.NameTableDoctor,
                listDoctName, ListItemForDoctName.class);
        //更新号种名称下拉列表
        updateOneComboBoxOfData(
        Config.NameTableCategoryRegister,
                listRegName, ListItemForRegName.class);

        ListItemForRegType itemSpecialist = new ListItemForRegType();
        ListItemForRegType itemNormal = new ListItemForRegType();
        itemSpecialist.isSpecialist = true;
        itemSpecialist.pronounce = "zhuanjiahao";
        itemNormal.isSpecialist = false;
        itemNormal.pronounce = "putonghao";
        listRegtype.clear();
        listRegtype.addAll(itemNormal, itemSpecialist);
    }
    //更新退款金额信息
    public void updateRefund() {
        int index = inputnamecat.getSelectionModel().getSelectedIndex();
        if (index == -1)
            return;
        if(usebalance.isSelected()) {
            labelrefund.setText("0￥");
            labelrefund.setStyle("");
            return;
        }
        if (!(inputpay.getText().trim().isEmpty())) {
            double inputmoney = Double.parseDouble(inputpay.getText().trim());
            double fee = listRegNameFiltered.get(index).fee;
            if (index != -1 && inputmoney > fee) {
                labelrefund.setText(String.format("%.2f￥", inputmoney - fee));
                labelrefund.setStyle("");
            } else {
                labelrefund.setText("交款金额不足");
                labelrefund.setStyle("-fx-text-fill: red;");
            }
        }
    }

    /**
     * 更新余额付款控件状态
     */
    public void updateUseBalance() {
        if (usebalance.isSelected()) {
            int index = inputnamecat.getSelectionModel().getSelectedIndex();
            if (index != -1 && PatientBalance < listRegNameFiltered.get(index).fee) {
                usebalance.setSelected(false);
                inputpay.setDisable(false);
                usebalance.setText("余额不足");
                usebalance.setDisable(true);
            } else {
                usebalance.setDisable(false);
                usebalance.setText("余额付款");
                usebalance.setSelected(true);
                inputpay.setDisable(true);
            }
        }
    }

    /**
     * 更新确定按钮的状态
     */
    private void updateButtonEnter() {
        button_enter.setDisable(true);
        int index;
        double money = inputpay.getText().trim().isEmpty()? 0.0 : Double.parseDouble(inputpay.getText().trim());
        //why inputdoctname index = -1?
        if(inputdoctname.getSelectionModel().getSelectedIndex() != -1 &&
                (index = inputnamecat.getSelectionModel().getSelectedIndex()) != -1 &&
                ((usebalance.isSelected() && PatientBalance >= listRegNameFiltered.get(index).fee) ||
                        (!usebalance.isSelected() && money >=
                                listRegNameFiltered.get(index).fee))) {
            button_enter.setDisable(false);

            System.out.println("able");
        } else
            System.out.println("disable: "+ inputdoctname.getSelectionModel().getSelectedIndex());
    }

    /**
     * 余额付款控件事件处理对象
     */
    public void useBalanceSelected() {
        if (usebalance.isSelected()) {
            inputpay.setDisable(true);
            updateRefund();
        } else {
            inputpay.setDisable(false);
            updateRefund();
        }
        updateButtonEnter();
        System.out.println("UseBalacne select");
    }



    private void reFilterDepartment(boolean withoutSelect) {
        int index;
        String previousKey = "";

        if((index = inputdep.getSelectionModel().getSelectedIndex()) != -1)
            previousKey = listDeptNameFiltered.get(index).number;

        ObservableList<ListItemForDepName> list0 = FXCollections.observableArrayList();
        ObservableList<ListItemForDepName> list1 = FXCollections.observableArrayList();

        //filter Department name

        ListItemForDepName tmp = null;
        try {
            for (ListItemForDepName item : listDeptName) {
                tmp = item;

                if (item.toString().contains(inputdep.getEditor().getText().trim()) ||
                        item.getPronounce().contains(inputdep.getEditor().getText().trim())) {
                    listDeptNameFiltered.add(item);
                    list0.add(item);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(tmp);
        }
        //filter again according to doctor
        if ((index = inputdoctname.getSelectionModel().getSelectedIndex()) != -1) {
            for(ListItemForDepName item : list0) {
                if(item.number.equals(listDoctNameFiltered.get(index).deptNum))
                    list1.add(item);
            }
            list0 = list1;
        }

        //add to filtered listDepartmentname and combobox
        boolean isInputValid = false;
        //newSelection 设置过滤后的list的index
        int counter = 0, newSelection = -1;
        inputdep.getItems().clear();
        listDeptNameFiltered.clear();
        for (ListItemForDepName item : list0) {
            inputdep.getItems().add(item.toString());
            listDeptNameFiltered.add(item);
            if(item.toString().contains(inputdep.getEditor().getText().trim()) ||
            item.getPronounce().contains(inputdep.getEditor().getText().trim())) {
                isInputValid = true;
            }
            if(previousKey.equals(item.number)) {
                newSelection = counter;
            }
            ++counter;
        }

        //clear invalid input
        if(!withoutSelect) {
            if(!isInputValid) {
                inputdep.getEditor().clear();
                System.out.println("Invalid input department name");
            }
            if(newSelection != -1) {
                inputdep.getSelectionModel().clearAndSelect(newSelection);
                inputdep.getEditor().setText((String) inputdep.getItems().get(newSelection));
            }
        }
    }

    private void reFilterDoctor(boolean withoutSelect) {
        int index;
        String previousKey = "";
        if((index = inputdoctname.getSelectionModel().getSelectedIndex()) != -1)
            previousKey = listDoctNameFiltered.get(index).number;

        ObservableList<ListItemForDoctName> list0 = FXCollections.observableArrayList();
        ObservableList<ListItemForDoctName> list1 = FXCollections.observableArrayList();

        //filter doctor name
        ListItemForDoctName tmp = null;
        try {
            for (ListItemForDoctName item : listDoctName) {

                tmp = item;

                if (item.toString().contains(inputdoctname.getEditor().getText().trim()) ||
                        item.getPronounce().contains(inputdoctname.getEditor().getText().trim())) {
//                    System.out.println(item);
                    list0.add(item);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("pronounced：" + tmp.getPronounce());
            System.out.println(tmp + inputdoctname.getEditor().getText());
        }

        //filter by department
        if((index = inputdep.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemForDoctName item : list0) {
                if (item.deptNum.equals(listDeptNameFiltered.get(index).number))
                    list1.add(item);
            }
            list0 = list1;
        }

        //filter by register type
        list1 = FXCollections.observableArrayList();
        if((index = inputtyperegister.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemForDoctName item : list0) {
                if (item.isSpecialist == listRegtypeFiltered.get(index).isSpecialist)
                    list1.add(item);
            }
            list0 = list1;
        }

        //filter bu register name
        list1 = FXCollections.observableArrayList();
        if ((index = inputnamecat.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemForDoctName item : list0) {
                if(item.deptNum.equals(listRegNameFiltered.get(index).department)) {
                    list1.add(item);
                }
                list0 = list1;
            }
        }

        //add to filtered listDoctor and comboBox
        boolean isInputValid = false;
        int counter = 0, newSelection = -1;
        inputdoctname.getItems().clear();
        listDoctNameFiltered.clear();
        for(ListItemForDoctName item : list0) {
            listDoctNameFiltered.add(item);
            inputdoctname.getItems().add(item.toString());

            if (item.toString().contains(inputdoctname.getEditor().getText().trim()) ||
            item.getPronounce().contains(inputdoctname.getEditor().getText().trim())){
                isInputValid = true;
            }
            if (previousKey.equals(item.number))
                newSelection = counter;
            ++counter;
        }
        //clear invalid input
        if(!withoutSelect) {
            if(!isInputValid) {
                System.out.println("invalid input doctor name：" + inputdoctname.getEditor().getText().trim());
                inputdoctname.getEditor().clear();
            }


            if(newSelection != -1){
                inputdoctname.getSelectionModel().clearAndSelect(newSelection);
                inputdoctname.getEditor().setText((String) inputdoctname.getItems().get(newSelection));
            }
        }
    }

    private void reFilterRegisterType(boolean withoutSelect) {
        int index;
        String previousKey = "";
        if((index = inputtyperegister.getSelectionModel().getSelectedIndex()) != -1){
            previousKey = listRegtypeFiltered.get(index).pronounce;
        }

        ObservableList<ListItemForRegType> list0 = FXCollections.observableArrayList();
        ObservableList<ListItemForRegType> list1 = FXCollections.observableArrayList();

        //filter register type
        ListItemForRegType tmp = null;
        try {
            for (ListItemForRegType item : listRegtype) {
                tmp = item;

                if (item.toString().contains(inputtyperegister.getEditor().getText().trim()) ||
                        item.getPronounce().contains(inputtyperegister.getEditor().getText().trim()))
                    list0.add(item);

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(tmp);
        }

        //filter by doctor
        if((index = inputdoctname.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemForRegType item : list0) {
                if (listDoctNameFiltered.get(index).isSpecialist == item.isSpecialist)

                    list1.add(item);
            }
            list0 = list1;
        }

        //filter by register name
        list1 = FXCollections.observableArrayList();
        if ((index = inputnamecat.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemForRegType item : list0) {
                if (item.isSpecialist == listRegNameFiltered.get(index).isSpecialist)
                    list1.add(item);
            }
            list0 = list1;
        }

        //add to filtered list and comboBox
        boolean isInputValid = false;
        int counter = 0, newSelecttion = -1;
        listRegtypeFiltered.clear();
        inputtyperegister.getItems().clear();
        for (ListItemForRegType item : list0) {
            listRegtypeFiltered.add(item);
            inputtyperegister.getItems().add(item.toString());
            if(item.toString().contains(inputtyperegister.getEditor().getText().trim()) ||
            item.getPronounce().contains(inputtyperegister.getEditor().getText().trim()))
                isInputValid = true;
            if(previousKey.equals(item.pronounce))
                newSelecttion = counter;
            ++counter;
        }

        //clear invalid input
        if(!withoutSelect) {
            if(!isInputValid) {
                inputtyperegister.getEditor().clear();
                System.out.println("invalid input register type");
            }
            if (newSelecttion != -1) {
                inputtyperegister.getSelectionModel().clearAndSelect(newSelecttion);
                inputtyperegister.getEditor().setText((String) inputtyperegister.getItems().get(newSelecttion));
            }
        }
    }

    private void reFilterRegisterName(boolean withoutSelect) {
        int index;
        String previousKey = "";
        if((index = inputnamecat.getSelectionModel().getSelectedIndex()) != -1) {
            previousKey = listRegNameFiltered.get(index).number;
        }

        ObservableList<ListItemForRegName> list0 = FXCollections.observableArrayList();
        ObservableList<ListItemForRegName> list1 = FXCollections.observableArrayList();

        //filter register name
        ListItemForRegName tmp = null;
        try {
            for (ListItemForRegName item : listRegName) {
                tmp = item;
                if (item.toString().contains(inputnamecat.getEditor().getText().trim()) ||
                        item.getPronounce().contains(inputnamecat.getEditor().getText().trim()))
                    list0.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(tmp);
        }

        //filtered by department
        if ((index = inputdep.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemForRegName item : list0) {
                if (item.department.equals(listDeptNameFiltered.get(index).number))
                    list1.add(item);
            }
            list0 = list1;
        }

        //filter by doctor name
        list1 = FXCollections.observableArrayList();
        if ((index = inputdoctname.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemForRegName item : list0) {
                if (item.isSpecialist == listDoctNameFiltered.get(index).isSpecialist)
                    list1.add(item);
            }
            list0 = list1;
        }

        //filter by register type
        list1 = FXCollections.observableArrayList();
        if ((index = inputtyperegister.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemForRegName item : list0) {
                if (item.isSpecialist == listRegtypeFiltered.get(index).isSpecialist)
                    list1.add(item);
            }
            list0 = list1;
        }

        //add to filtered list and comboBox
        boolean isInputValid = false;
        int counter = 0, newSelecttion = -1;
        listRegNameFiltered.clear();
        inputnamecat.getItems().clear();
        for (ListItemForRegName item : list0) {
            listRegNameFiltered.add(item);
            inputnamecat.getItems().add(item.toString());
            if(item.toString().contains(inputnamecat.getEditor().getText().trim()) ||
            item.getPronounce().contains(inputnamecat.getEditor().getText().trim()))
                isInputValid = true;
            if(previousKey.equals(item.number))
                newSelecttion = counter;
            ++counter;
        }

        //clear invalid input
        if(!withoutSelect) {
            if(!isInputValid) {
                inputnamecat.getEditor().clear();
                System.out.println("invalid input register name_cat");
            }
            if(newSelecttion != -1) {
                inputnamecat.getSelectionModel().clearAndSelect(newSelecttion);
                inputnamecat.getEditor().setText((String) inputnamecat.getItems().get(newSelecttion));
            }
        }
    }

    private boolean shouldPassKeyCode(KeyCode code) {
        return code == KeyCode.DOWN ||
                code == KeyCode.UP ||
                code == KeyCode.ENTER;
    }
    /**
     * 检测按键
     */
    public void keyRelease() {
        button_enter.setOnKeyReleased(KeyEvent -> {
            if (KeyEvent.getCode() == KeyCode.ENTER)
                on_pushButtonEnter_clicked();
        });
        button_clear.setOnKeyReleased(KeyEvent -> {
            if (KeyEvent.getCode() == KeyCode.ENTER)
                on_pushButtonClear_clicked();
        });
        button_exit.setOnKeyReleased(KeyEvent -> {
            if (KeyEvent.getCode() == KeyCode.ENTER) {
                try {
                    on_pushButtonExit_clicked();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void on_pushButtonEnter_clicked() {
        System.out.println("button enter clicked!");

        //进行挂号，获取挂号编号，这一阶段不接受任何输入
        mainPane.setDisable(true);
        int index = inputnamecat.getSelectionModel().getSelectedIndex();
        RegisterService service = new RegisterService(
                listRegNameFiltered.get(index).number,
                listDoctNameFiltered.get(inputdoctname.getSelectionModel().getSelectedIndex()).number,
                PatientNumber,
                listRegNameFiltered.get(index).fee,
                usebalance.isSelected(),
                ((!usebalance.isSelected() && addTobalance.isSelected()) ?
                        Double.parseDouble(inputpay.getText().trim()) - listRegNameFiltered.get(index).fee : 0));

        service.setOnSucceeded(WorkerStateEvent -> {
            switch (service.returnCode) {
                case registerCategoryNotFound:
                case sqlException:
                    labelStatus.setText("数据库错误, 请检查后重新尝试!");
                    labelStatus.setStyle("-fx-text-fill: red;");
                    break;
                case registerNumberExceeded:
                    labelStatus.setText("此号已达到人数上限.");
                    labelStatus.setStyle("-fx-text-fill: red;");
                    break;
                case retryTimeExceeded:
                    labelStatus.setText("系统繁忙，请稍后再试!");
                    labelStatus.setStyle("-fx-text-fill: red;");
                case noError:
                    labelStatus.setText("挂号成功");
                    labelregnum.setText(String.valueOf(service.registerNum));
                    PatientBalance = service.updateBalance;
                    updateDisplayInfo();
                    break;
            }
            mainPane.setDisable(false);
        });
        service.start();
    }
    public void on_pushButtonClear_clicked() {
        inputdep.getItems().clear();
        inputdoctname.getItems().clear();
        inputtyperegister.getItems().clear();
        inputnamecat.getItems().clear();
        reFilterDepartment(false);
        reFilterDoctor(false);
        reFilterRegisterType(false);
        reFilterRegisterName(false);

        System.out.println("button clear clicked!");
    }

    public void on_pushButtonExit_clicked() throws IOException {
        System.out.println("button exit clicked!");
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/view/login.fxml")));
        FXRobotHelper.getStages().get(0).setScene(scene);
    }

}

final class RegisterService extends Service {
    private String registerCategoryNumber, doctorNum, patientNum;
    private Double registerFee;
    private boolean deductFromBalance;
    private Double addTobalance;

    private int retry = 5;

    //return value
    int registerNum;
    RegisterExcption.ErrorCode returnCode;
    double updateBalance;

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public RegisterService(String registerCategoryNumber, String doctorNum,
                           String patientNum, Double registerFee,
                           boolean deductFromBalance, Double addTobalance) {
        this.registerCategoryNumber = registerCategoryNumber;
        this.doctorNum = doctorNum;
        this.patientNum = patientNum;
        this.registerFee = registerFee;
        this.deductFromBalance = deductFromBalance;
        this.addTobalance = addTobalance;
    }

    @Override
    protected Task createTask() {

        return new Task() {
            @Override
            protected Object call() throws Exception {
                int i;
                for ( i = 0; i < retry; ++i) {
                    try {
                        registerNum = DBConnector.getInstance().register(registerCategoryNumber,
                                doctorNum, patientNum, registerFee,
                                deductFromBalance, addTobalance);
                        System.out.println("register num: " + registerNum);

                        break;  //正常情况下挂号成功，则直接跳出循环不需要重试
                    } catch (RegisterExcption registerExcption) {
                        switch (registerExcption.error) {
                            //数据库连接错误则重试
                            case sqlException:
                                returnCode = RegisterExcption.ErrorCode.sqlException;
                                break;
                            default:
                                returnCode = registerExcption.error;
                                return null;
                        }
                    }
                }
                if (i == retry)
                    returnCode = RegisterExcption.ErrorCode.retryTimeExceeded;
                else {
                    returnCode = RegisterExcption.ErrorCode.noError;

                    ResultSet result = DBConnector.getInstance().getPatientInfo(patientNum);
                    if(!result.next())
                        returnCode = RegisterExcption.ErrorCode.patientNotExist;
                    updateBalance = result.getDouble(Config.NameTableColumnPatientBalance);
                    System.out.println("Balance: " + updateBalance);
                }
                return null;
            }
        };
    }
}
