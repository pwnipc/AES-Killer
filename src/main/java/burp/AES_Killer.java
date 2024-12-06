/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 *
 * @author 
 */
public class AES_Killer extends javax.swing.JPanel {

    /**
     * Creates new form AES_Killer
     */

    BurpExtender _burpObj;

    public AES_Killer(BurpExtender _b) {
        this._burpObj = _b;
        initComponents();

        // Initialize default states for checkboxes
        this.jCheckBox9.setSelected(true);
        this.jCheckBox10.setSelected(true);
        this.jCheckBox11.setSelected(true);
        this.jCheckBox12.setSelected(true);

        // Disable certain checkboxes as per original code
        this.jCheckBox9.setEnabled(false);
        this.jCheckBox10.setEnabled(false);
        this.jCheckBox11.setEnabled(false);
        this.jCheckBox12.setEnabled(false);

        // Add action listener to encryption type ComboBox
        this.jComboBox1.addActionListener(e -> handleEncryptionTypeChange());

        // Load existing configuration, if any
        loadConfig();
    }

    /**
     * Handles UI changes based on the selected encryption type.
     */
    private void handleEncryptionTypeChange() {
        String selectedEncType = String.valueOf(this.jComboBox1.getSelectedItem());

        if (selectedEncType.equals("AES/ECB/NoPadding")) {
            // Automatically exclude IV for ECB mode
            this.jCheckBox1.setSelected(true);
            this.jCheckBox1.setEnabled(false); // Prevent user from changing
            this.jTextField2.setEnabled(false); // Disable IV input
        } else {
            // Enable IV options for other modes
            this.jCheckBox1.setEnabled(true);
            // If "Exclude IV" is not selected, enable IV input
            if (!this.jCheckBox1.isSelected()) {
                this.jTextField2.setEnabled(true);
            } else {
                this.jTextField2.setEnabled(false);
            }
        }
    }

    /**
     * Loads the extension configuration from Burp's settings.
     */
    private void loadConfig() {

        String AesKillerConfig = _burpObj.callbacks.loadExtensionSetting("AES_Killer_Data");

        if (AesKillerConfig == null || AesKillerConfig.trim().isEmpty()) {
            _burpObj.callbacks.printOutput("No existing AES_Killer configuration found. Using default settings.");
            return; // No configuration to load
        }

        try {
            Gson gson = new Gson();
            Type confMapType = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> map = gson.fromJson(AesKillerConfig, confMapType);

            if (map == null) {
                _burpObj.callbacks.printError("AES_Killer: Configuration map is null.");
                return;
            }

            // Safely retrieve and set each configuration parameter
            setTextField(jTextField1, map, "jTextField1"); // Secret Key
            setTextField(jTextField2, map, "jTextField2"); // IV
            setTextField(jTextField3, map, "jTextField3"); // Request Parameter
            setTextField(jTextField4, map, "jTextField4"); // Response Parameter
            setTextField(jTextField5, map, "jTextField5"); // Obfuscated Characters
            setTextField(jTextField6, map, "jTextField6"); // Replace Characters
            setTextField(jTextField7, map, "jTextField7"); // Host URL

            setCheckBox(jCheckBox1, map, "jCheckBox1"); // Exclude IV
            setCheckBox(jCheckBox2, map, "jCheckBox2"); // Complete Request Body
            setCheckBox(jCheckBox3, map, "jCheckBox3"); // Specific Request Parameters
            setCheckBox(jCheckBox4, map, "jCheckBox4"); // Complete Response Body
            setCheckBox(jCheckBox5, map, "jCheckBox5"); // Specific Response Parameters
            setCheckBox(jCheckBox6, map, "jCheckBox6"); // Override Request Body - Form
            setCheckBox(jCheckBox7, map, "jCheckBox7"); // Override Response Body - Form
            setCheckBox(jCheckBox8, map, "jCheckBox8"); // Do/Remove Obfuscation
            setCheckBox(jCheckBox9, map, "jCheckBox9"); // Proxy
            setCheckBox(jCheckBox10, map, "jCheckBox10"); // Repeater
            setCheckBox(jCheckBox11, map, "jCheckBox11"); // Scanner
            setCheckBox(jCheckBox12, map, "jCheckBox12"); // Intruder
            setCheckBox(jCheckBox13, map, "jCheckBox13"); // Enable Debug Mode
            setCheckBox(jCheckBox14, map, "jCheckBox14"); // URL encode/decode
            setCheckBox(jCheckBox15, map, "jCheckBox15"); // Ignore Response
            setCheckBox(jCheckBox16, map, "jCheckBox16"); // Override Request Body - JSON
            setCheckBox(jCheckBox17, map, "jCheckBox17"); // Override Response Body - JSON
            setCheckBox(jCheckBox18, map, "jCheckBox18"); // Req/Resp tab

            // Set ComboBox selection
            if (map.get("jComboBox1") != null) {
                String encType = map.get("jComboBox1").toString();
                jComboBox1.setSelectedItem(encType);
            }

            // After setting the encryption type, handle UI changes accordingly
            handleEncryptionTypeChange();

            _burpObj.callbacks.printOutput("AESKiller config loaded successfully!");
        } catch (RuntimeException e) {
            _burpObj.callbacks.printError("AES_Killer: Error loading configuration - " + e.toString());
            _burpObj.callbacks.printOutput("Error loading AESKiller config!");
        }
    }

    /**
     * Helper method to safely set text fields from the configuration map.
     */
    private void setTextField(javax.swing.JTextField textField, Map<String, Object> map, String key) {
        if (map.get(key) != null) {
            textField.setText(map.get(key).toString());
        } else {
            _burpObj.callbacks.printOutput("AES_Killer: Missing config key for " + key + ". Using default value.");
        }
    }

    /**
     * Helper method to safely set checkboxes from the configuration map.
     */
    private void setCheckBox(javax.swing.JCheckBox checkBox, Map<String, Object> map, String key) {
        if (map.get(key) != null) {
            boolean isSelected = Boolean.parseBoolean(map.get(key).toString());
            checkBox.setSelected(isSelected);
            // If encryption type is AES/ECB/NoPadding, ensure related UI components are disabled
            if (key.equals("jCheckBox1") && isSelected && String.valueOf(this.jComboBox1.getSelectedItem()).equals("AES/ECB/NoPadding")) {
                checkBox.setEnabled(false);
                this.jTextField2.setEnabled(false);
            }
        } else {
            _burpObj.callbacks.printOutput("AES_Killer: Missing config key for " + key + ". Using default state.");
        }
    }

    /**
     * Saves the current configuration to Burp's settings.
     */
    private void saveConfig() {
        try {
            Map<String, Object> map = new HashMap<>();
            // Populate the map with current UI values
            map.put("jTextField1", jTextField1.getText()); // Secret Key
            map.put("jTextField2", jTextField2.getText()); // IV
            map.put("jTextField3", jTextField3.getText()); // Request Parameter
            map.put("jTextField4", jTextField4.getText()); // Response Parameter
            map.put("jTextField5", jTextField5.getText()); // Obfuscated Characters
            map.put("jTextField6", jTextField6.getText()); // Replace Characters
            map.put("jTextField7", jTextField7.getText()); // Host URL

            map.put("jCheckBox1", jCheckBox1.isSelected()); // Exclude IV
            map.put("jCheckBox2", jCheckBox2.isSelected()); // Complete Request Body
            map.put("jCheckBox3", jCheckBox3.isSelected()); // Specific Request Parameters
            map.put("jCheckBox4", jCheckBox4.isSelected()); // Complete Response Body
            map.put("jCheckBox5", jCheckBox5.isSelected()); // Specific Response Parameters
            map.put("jCheckBox6", jCheckBox6.isSelected()); // Override Request Body - Form
            map.put("jCheckBox7", jCheckBox7.isSelected()); // Override Response Body - Form
            map.put("jCheckBox8", jCheckBox8.isSelected()); // Do/Remove Obfuscation
            map.put("jCheckBox9", jCheckBox9.isSelected()); // Proxy
            map.put("jCheckBox10", jCheckBox10.isSelected()); // Repeater
            map.put("jCheckBox11", jCheckBox11.isSelected()); // Scanner
            map.put("jCheckBox12", jCheckBox12.isSelected()); // Intruder
            map.put("jCheckBox13", jCheckBox13.isSelected()); // Enable Debug Mode
            map.put("jCheckBox14", jCheckBox14.isSelected()); // URL encode/decode
            map.put("jCheckBox15", jCheckBox15.isSelected()); // Ignore Response
            map.put("jCheckBox16", jCheckBox16.isSelected()); // Override Request Body - JSON
            map.put("jCheckBox17", jCheckBox17.isSelected()); // Override Response Body - JSON
            map.put("jCheckBox18", jCheckBox18.isSelected()); // Req/Resp tab

            map.put("jComboBox1", jComboBox1.getSelectedItem()); // Encryption Type

            String AesKillerConfig = new Gson().toJson(map);

            _burpObj.callbacks.saveExtensionSetting("AES_Killer_Data", AesKillerConfig);
            _burpObj.callbacks.printOutput("AESKiller config saved successfully!");
        } catch (RuntimeException e) {
            _burpObj.callbacks.printError("AES_Killer: Error saving configuration - " + e.toString());
        }
    }

    /**
     * Utility method to check if a string is empty or null.
     */
    public Boolean is_string_empty(String _str) {
        return (_str == null || _str.trim().isEmpty());
    }

    /**
     * Validates the host URL provided by the user.
     */
    public Boolean validate_host() {
        String _url = this.jTextField7.getText().trim();
        if (is_string_empty(_url)) {
            JOptionPane.showMessageDialog(this, "Please provide a part of Host domain (e.g., https://example.com) !!!");
            return false;
        }

        try {
            URL abc = new URL(_url);
            this._burpObj._host = abc.getHost();
            return true;
        } catch (Exception ex) {
            // If URL parsing fails, assume the user provided a host without scheme
            this._burpObj._host = _url;
            return true;
        }
    }

    /**
     * Validates the secret key input.
     */
    public Boolean validate_secret_key() {
        String _secret_key = this.jTextField1.getText().trim();
        if (is_string_empty(_secret_key)) {
            JOptionPane.showMessageDialog(this, "Please provide a Secret Key (Base64 Encoded) !!!");
            return false;
        }
        this._burpObj._secret_key = _secret_key;
        return true;
    }

    /**
     * Validates the IV parameter input.
     */
    public Boolean validate_iv_param() {
        String selectedEncType = String.valueOf(this.jComboBox1.getSelectedItem());
        if (selectedEncType.equals("AES/ECB/NoPadding")) {
            this._burpObj._exclude_iv = true;
            return true;
        }

        if (this.jCheckBox1.isSelected()) {
            this._burpObj._exclude_iv = true;
            this.jTextField2.setEnabled(false);
            return true;
        }

        String _iv_param = this.jTextField2.getText().trim();
        if (is_string_empty(_iv_param)) {
            JOptionPane.showMessageDialog(this, "Please provide an IV Parameter (Base64 Encoded) !!!");
            return false;
        }
        this._burpObj._iv_param = _iv_param;
        this._burpObj._exclude_iv = false;
        return true;
    }

    /**
     * Validates obfuscation settings.
     */
    public Boolean validate_Obff() {
        if (!this.jCheckBox8.isSelected()) {
            this._burpObj._do_off = false;
            return true;
        }

        String _obff_char = this.jTextField5.getText().trim();
        if (is_string_empty(_obff_char)) {
            JOptionPane.showMessageDialog(this, "Please provide Obfuscated characters (Separated by space) !!!");
            return false;
        }

        String _replace_with = this.jTextField6.getText().trim();
        if (is_string_empty(_replace_with)) {
            JOptionPane.showMessageDialog(this, "Please provide characters to replace with (Separated by space) !!!");
            return false;
        }

        String[] obfChars = _obff_char.split(" ");
        String[] repChars = _replace_with.split(" ");

        if (obfChars.length != repChars.length) {
            JOptionPane.showMessageDialog(this, "The number of obfuscated characters and replacement characters must match !!!");
            return false;
        }

        this._burpObj._obffusicatedChar = obfChars;
        this._burpObj._replaceWithChar = repChars;
        this._burpObj._do_off = true;
        return true;
    }

    /**
     * Validates URL encoding/decoding setting.
     */
    public Boolean validate_url_ed() {
        this._burpObj._url_enc_dec = this.jCheckBox14.isSelected();
        return true;
    }

    /**
     * Validates and sets the debug mode.
     */
    public Boolean validate_debug_mode() {
        this._burpObj.isDebug = this.jCheckBox13.isSelected();
        return true;
    }

    /**
     * Validates the request/response tab setting.
     */
    public Boolean validate_req_tab() {
        this._burpObj._req_tab = this.jCheckBox18.isSelected();
        return true;
    }

    /**
     * Validates the request parameters settings.
     */
    public Boolean validate_request_params() {
        String selectedEncType = String.valueOf(this.jComboBox1.getSelectedItem());

        if (this.jCheckBox2.isSelected()) {
            this._burpObj._is_req_body = true;
            this._burpObj._is_ovrr_req_body = false;
            this._burpObj._is_req_param = false;
            return true;
        } else if (this.jCheckBox3.isSelected()) {
            this._burpObj._is_req_body = false;
            this._burpObj._is_req_param = true;
            this._burpObj._is_ovrr_req_body = false;

            if (this.jCheckBox6.isSelected()) {
                this._burpObj._is_ovrr_req_body = true;
                this._burpObj._is_ovrr_req_body_form = true;
                this._burpObj._is_ovrr_req_body_json = false;
            }
            if (this.jCheckBox16.isSelected()) {
                this._burpObj._is_ovrr_req_body = true;
                this._burpObj._is_ovrr_req_body_json = true;
                this._burpObj._is_ovrr_req_body_form = false;
            }

            String _req_param = this.jTextField3.getText().trim();
            if (is_string_empty(_req_param)) {
                JOptionPane.showMessageDialog(this, "Please provide Request Parameter(s) (Separated by space) !!!");
                return false;
            }

            this._burpObj._req_param = _req_param.split(" ");
            if (this._burpObj._is_ovrr_req_body && this._burpObj._req_param.length > 1) {
                JOptionPane.showMessageDialog(this, "Request Parameters can't exceed one when overriding the body !!!");
                return false;
            }

            // Additional validation for NoPadding mode
            if (selectedEncType.equals("AES/ECB/NoPadding")) {
                // Inform the user about padding requirements
                JOptionPane.showMessageDialog(this, "AES/ECB/NoPadding requires that the plaintext length be a multiple of 16 bytes.");
            }

            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Please select Request Options !!!");
            return false;
        }
    }

    /**
     * Validates the response parameters settings.
     */
    public Boolean validate_response_params() {
        if (this.jCheckBox15.isSelected()) {
            this._burpObj._is_res_body = false;
            this._burpObj._is_ovrr_res_body = false;
            this._burpObj._is_res_param = false;
            return true;
        } else if (this.jCheckBox4.isSelected()) {
            this._burpObj._is_res_body = true;
            this._burpObj._is_ovrr_res_body = false;
            this._burpObj._is_res_param = false;
            return true;
        } else if (this.jCheckBox5.isSelected()) {
            this._burpObj._is_res_body = false;
            this._burpObj._is_ovrr_res_body = false;
            this._burpObj._is_res_param = true;

            if (this.jCheckBox7.isSelected()) {
                this._burpObj._is_ovrr_res_body = true;
                this._burpObj._is_ovrr_res_body_form = true;
                this._burpObj._is_ovrr_res_body_json = false;
            }
            if (this.jCheckBox17.isSelected()) {
                this._burpObj._is_ovrr_res_body = true;
                this._burpObj._is_ovrr_res_body_json = true;
                this._burpObj._is_ovrr_res_body_form = false;
            }

            String _res_param = this.jTextField4.getText().trim();
            if (is_string_empty(_res_param)) {
                JOptionPane.showMessageDialog(this, "Please provide Response Parameter(s) (Separated by space) !!!");
                return false;
            }

            this._burpObj._res_param = _res_param.split(" ");
            if (this._burpObj._is_ovrr_res_body && this._burpObj._res_param.length > 1) {
                JOptionPane.showMessageDialog(this, "Response Parameters can't exceed one when overriding the body !!!");
                return false;
            }
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Please select Response Options !!!");
            return false;
        }
    }

    /**
     * Action performed when the "Start AES Killer" button is clicked.
     */
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        // Validate Host
        if (!validate_host()) {
            return;
        }

        // Validate encryption/decryption input
        this._burpObj._enc_type = String.valueOf(this.jComboBox1.getSelectedItem());
        if (!validate_secret_key()) {
            return;
        }
        if (!validate_iv_param()) {
            return;
        }

        // Validate Obfuscation and URL encoding/decoding
        if (!validate_Obff()) {
            return;
        }
        if (!validate_url_ed()) {
            return;
        }

        // Validate Debug Mode
        validate_debug_mode();

        // Validate request/response tabs
        validate_req_tab();

        // Validate Request Parameters
        if (!validate_request_params()) {
            return;
        }

        // Validate Response Parameters
        if (!validate_response_params()) {
            return;
        }

        // Start AES Killer
        this._burpObj.start_aes_killer();

        // Toggle buttons
        this.jButton2.setEnabled(false);
        this.jButton1.setEnabled(true);

        // Save configuration
        saveConfig();

        JOptionPane.showMessageDialog(this, "AES Killer started successfully!");
    }

    /**
     * Action performed when the "Stop AES Killer" button is clicked.
     */
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        this._burpObj.stop_aes_killer();

        this.jButton2.setEnabled(true);
        this.jButton1.setEnabled(false);

        JOptionPane.showMessageDialog(this, "AES Killer stopped successfully!");
    }

    /**
     * Action performed when the "Clear" button is clicked.
     */
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {
        this.jTextArea1.setText("");
        this.jTextArea2.setText("");
    }

    /**
     * Action performed when the "Encrypt" button is clicked.
     */
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
        String _txt = this.jTextArea1.getText().trim();
        if (is_string_empty(_txt)) {
            JOptionPane.showMessageDialog(this, "Please provide data to encrypt !!!");
            return;
        }

        // Ensure encryption settings are validated
        this._burpObj._enc_type = String.valueOf(this.jComboBox1.getSelectedItem());
        if (!validate_secret_key()) {
            return;
        }
        if (!validate_iv_param()) {
            return;
        }
        if (!validate_Obff()) {
            return;
        }
        if (!validate_url_ed()) {
            return;
        }

        // Additional check for NoPadding mode
        String selectedEncType = String.valueOf(this.jComboBox1.getSelectedItem());
        if (selectedEncType.equals("AES/ECB/NoPadding")) {
            if (_txt.length() % 16 != 0) {
                JOptionPane.showMessageDialog(this, "For AES/ECB/NoPadding, the input length must be a multiple of 16 bytes.");
                return;
            }
        }

        // Perform encryption
        String encryptedText = this._burpObj.do_encrypt(_txt);
        this.jTextArea2.setText(encryptedText);
    }

    /**
     * Action performed when the "Decrypt" button is clicked.
     */
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {
        String _txt = this.jTextArea1.getText().trim();
        if (is_string_empty(_txt)) {
            JOptionPane.showMessageDialog(this, "Please provide data to decrypt !!!");
            return;
        }

        // Ensure decryption settings are validated
        this._burpObj._enc_type = String.valueOf(this.jComboBox1.getSelectedItem());
        if (!validate_secret_key()) {
            return;
        }
        if (!validate_iv_param()) {
            return;
        }
        if (!validate_Obff()) {
            return;
        }
        if (!validate_url_ed()) {
            return;
        }

        // Perform decryption
        String decryptedText = this._burpObj.do_decrypt(_txt);
        this.jTextArea2.setText(decryptedText);
    }

    // Auto-generated UI initialization code remains unchanged.
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jCheckBox9 = new javax.swing.JCheckBox();
        jCheckBox10 = new javax.swing.JCheckBox();
        jCheckBox11 = new javax.swing.JCheckBox();
        jCheckBox12 = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jPanel12 = new javax.swing.JPanel();
        jCheckBox8 = new javax.swing.JCheckBox();
        jCheckBox13 = new javax.swing.JCheckBox();
        jCheckBox14 = new javax.swing.JCheckBox();
        jCheckBox18 = new javax.swing.JCheckBox();
        //jCheckBox19 = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel9 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jTextField3 = new javax.swing.JTextField();
        jCheckBox6 = new javax.swing.JCheckBox();
        jCheckBox16 = new javax.swing.JCheckBox();
        jPanel8 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jCheckBox4 = new javax.swing.JCheckBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        jTextField4 = new javax.swing.JTextField();
        jCheckBox7 = new javax.swing.JCheckBox();
        jCheckBox15 = new javax.swing.JCheckBox();
        jCheckBox17 = new javax.swing.JCheckBox();
        jPanel11 = new javax.swing.JPanel();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jTabbedPane4 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jSplitPane1.setDividerLocation(440);
        jSplitPane1.setDividerSize(20);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jButton1.setText("Stop AES Killer");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Start AES Killer");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Encrypt");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Decrypt");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("Clear");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel10.setEnabled(false);

        jCheckBox9.setText("Proxy");

        jCheckBox10.setText("Repeater");

        jCheckBox11.setText("Scanner");

        jCheckBox12.setText("Intruder");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jCheckBox9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBox11))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jCheckBox10)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBox12)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox9)
                    .addComponent(jCheckBox11))
                .addGap(18, 18, 18)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox10)
                    .addComponent(jCheckBox12))
                .addGap(16, 16, 16))
        );

        jLabel8.setText("Host URL");

        jTextField7.setName("host_url"); // NOI18N

        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jCheckBox8.setText("Do / Remove Obfuscation");

        jCheckBox13.setText("Enable Debug Mode");
        jCheckBox13.setName("isDebug"); // NOI18N

        jCheckBox14.setText("URL encode/decode");

        jCheckBox18.setText("Req/Resp tab");

        //jCheckBox19.setText("Response tab");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox8)
                    .addComponent(jCheckBox14)
                    .addComponent(jCheckBox13))
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox18))
                    //.addComponent(jCheckBox19))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel12Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jCheckBox8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBox13))
                .addGroup(jPanel12Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jCheckBox18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        //.addComponent(jCheckBox19)
                        .addContainerGap())
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jButton3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton4)
                                .addGap(18, 18, 18)
                                .addComponent(jButton5))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(39, 39, 39)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jTextField7))
                        .addComponent(jLabel8))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 430, Short.MAX_VALUE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addGap(26, 26, 26)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel8)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton2)
                        .addComponent(jButton1))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton3)
                        .addComponent(jButton4)
                        .addComponent(jButton5))
                    .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel4.setLayout(null);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel1.setText("Select Encryption");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { 
            "AES/CBC/PKCS5Padding", 
            "AES/ECB/PKCS5Padding", 
            "AES/ECB/NoPadding", // Newly added
            "GOST3412-2015/ECB/PKCS7Padding", 
            "AES/GCM/NoPadding" 
        }));
        jComboBox1.setName("encryption_type"); // NOI18N

        jLabel2.setText("Secret Key (Base64 Encoded)");

        jTextField1.setName("secretKey"); // NOI18N

        jLabel3.setText("IV (Base64 Encoded)");

        jTextField2.setName("iv"); // NOI18N

        jCheckBox1.setText("Exclude / Ignore IV");
        jCheckBox1.setName("excludeIV"); // NOI18N
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField1)
                        .addComponent(jTextField2)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1)
                                .addComponent(jLabel2)
                                .addComponent(jLabel3)
                                .addComponent(jCheckBox1))
                            .addGap(0, 204, Short.MAX_VALUE)))
                    .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel1)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(jLabel2)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(jLabel3)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(jCheckBox1)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.add(jPanel5);
        jPanel5.setBounds(8, 7, 423, 270);

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel6.setText("Obfuscated Characters (Separated by space)");

        jTextField5.setName("off_char"); // NOI18N

        jLabel7.setText("Replace with Characters (Separated by space)");

        jTextField6.setName("replace_with"); // NOI18N

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel9Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField5))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(13, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel9Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6)
                        .addComponent(jLabel7))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.add(jPanel9);
        jPanel9.setBounds(450, 220, 540, 58);

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel4.setText("Request Options to Decrypt & Encrypt");

        buttonGroup2.add(jCheckBox2);
        jCheckBox2.setText("Complete Request Body");
        jCheckBox2.setName("req_body"); // NOI18N

        buttonGroup2.add(jCheckBox3);
        jCheckBox3.setText("Specific Request Parameters (Separated by space)");
        jCheckBox3.setName("specific_req_params"); // NOI18N

        jTextField3.setName("req_parameter"); // NOI18N

        //buttonGroup3.add(jCheckBox6);
        jCheckBox6.setText("Override Complete Request Body (After Decrypting - Form)");
        jCheckBox6.setName("override_req_form"); // NOI18N

        //buttonGroup3.add(jCheckBox16);
        jCheckBox16.setText("Override Complete Request Body (After Decrypting - JSON)");
        jCheckBox16.setName("override_req_json"); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jCheckBox2)
                        .addComponent(jCheckBox3)
                        .addComponent(jTextField3)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jCheckBox6)
                        .addComponent(jCheckBox16))
                    .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel4)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jCheckBox2)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jCheckBox3)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(jCheckBox6)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jCheckBox16)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.add(jPanel7);
        jPanel7.setBounds(450, 10, 450, 200);

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel5.setText("Response Options to Decrypt & Encrypt");

        buttonGroup1.add(jCheckBox4);
        jCheckBox4.setText("Complete Response Body");
        jCheckBox4.setName("res_body"); // NOI18N

        buttonGroup1.add(jCheckBox5);
        jCheckBox5.setText("Specific Response Parameters (Separated by space)");
        jCheckBox5.setName("specific_res_params"); // NOI18N

        jTextField4.setName("res_parameter"); // NOI18N

        //buttonGroup4.add(jCheckBox7);
        jCheckBox7.setText("Override Complete Response Body (After Decrypting - Form)");
        jCheckBox7.setName("override_res_form"); // NOI18N
        jCheckBox7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox7ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jCheckBox15);
        jCheckBox15.setText("Ignore Response");
        jCheckBox15.setName("ignore_response"); // NOI18N

        //buttonGroup4.add(jCheckBox17);
        jCheckBox17.setText("Override Complete Response Body (After Decrypting - JSON)");
        jCheckBox17.setName("override_res_json"); // NOI18N
        jCheckBox17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox17ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel8Layout.createSequentialGroup()
                            .addComponent(jCheckBox4)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jCheckBox15))
                        .addGroup(jPanel8Layout.createSequentialGroup()
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 204, Short.MAX_VALUE))
                        .addGroup(jPanel8Layout.createSequentialGroup()
                            .addComponent(jCheckBox5)
                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jPanel8Layout.createSequentialGroup()
                            .addComponent(jCheckBox7)
                            .addGap(0, 0, Short.MAX_VALUE))
                        .addGroup(jPanel8Layout.createSequentialGroup()
                            .addComponent(jTextField4)
                            .addContainerGap())
                        .addGroup(jPanel8Layout.createSequentialGroup()
                            .addComponent(jCheckBox17)
                            .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel5)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jCheckBox4)
                        .addComponent(jCheckBox15))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jCheckBox5)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(jCheckBox7)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jCheckBox17)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.add(jPanel8);
        jPanel8.setBounds(910, 10, 460, 200);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane1.setLeftComponent(jPanel1);

        jPanel11.setLayout(new javax.swing.BoxLayout(jPanel11, javax.swing.BoxLayout.LINE_AXIS));

        jTabbedPane3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jPanel3.add(jScrollPane1);

        jTabbedPane3.addTab("Input", jPanel3);

        jPanel11.add(jTabbedPane3);

        jTabbedPane4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jPanel2.setLayout(new java.awt.BorderLayout());

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane2.setViewportView(jTextArea2);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jTabbedPane4.addTab("Output", jPanel2);

        jPanel11.add(jTabbedPane4);

        jSplitPane1.setRightComponent(jPanel11);

        add(jSplitPane1);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Action performed when jCheckBox7 is clicked.
     * (Override Complete Response Body - Form)
     */
    private void jCheckBox7ActionPerformed(java.awt.event.ActionEvent evt) {
        // Additional actions can be added here if needed
    }

    /**
     * Action performed when jCheckBox17 is clicked.
     * (Override Complete Response Body - JSON)
     */
    private void jCheckBox17ActionPerformed(java.awt.event.ActionEvent evt) {
        // Additional actions can be added here if needed
    }

    /**
     * Action performed when jCheckBox1 is clicked.
     * (Exclude / Ignore IV)
     */
    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (this.jCheckBox1.isSelected()) {
            this.jTextField2.setEnabled(false);
        } else {
            String selectedEncType = String.valueOf(this.jComboBox1.getSelectedItem());
            if (!selectedEncType.equals("AES/ECB/NoPadding")) {
                this.jTextField2.setEnabled(true);
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox10;
    private javax.swing.JCheckBox jCheckBox11;
    private javax.swing.JCheckBox jCheckBox12;
    private javax.swing.JCheckBox jCheckBox13;
    private javax.swing.JCheckBox jCheckBox14;
    private javax.swing.JCheckBox jCheckBox15;
    private javax.swing.JCheckBox jCheckBox16;
    private javax.swing.JCheckBox jCheckBox17;
    private javax.swing.JCheckBox jCheckBox18;
    //private javax.swing.JCheckBox jCheckBox19;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JCheckBox jCheckBox7;
    private javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JCheckBox jCheckBox9; // Ensure this line is present
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTabbedPane jTabbedPane4;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    // End of variables declaration//GEN-END:variables
}
