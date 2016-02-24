/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bluetooth.le;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static String VALUE_SERVICE = "0000ff01-0000-1000-8000-00805f9b34fb";

    public static String ADC_SERVICE = "0000ff10-0000-1000-8000-00805f9b34fb";
    public static String ADC_ONOFF_CHARACTERISTIC = "0000ff11-0000-1000-8000-00805f9b34fb";
    public static String ADC_TIME_CHARACTERISTIC = "0000ff12-0000-1000-8000-00805f9b34fb";
    public static String ADC1_CHARACTERISTIC = "0000ff13-0000-1000-8000-00805f9b34fb";
    public static String ADC2_CHARACTERISTIC = "0000ff14-0000-1000-8000-00805f9b34fb";
    
    public static String PWM_SERVICE = "0000ff20-0000-1000-8000-00805f9b34fb";
    public static String PWM_CFG_CHARACTERISTIC = "0000ff21-0000-1000-8000-00805f9b34fb";
    public static String PWM1_CHARACTERISTIC = "0000ff22-0000-1000-8000-00805f9b34fb";
    public static String PWM2_CHARACTERISTIC = "0000ff23-0000-1000-8000-00805f9b34fb";
    public static String PWM3_CHARACTERISTIC = "0000ff24-0000-1000-8000-00805f9b34fb";
    
    
    
    public static String GPIO_SERVICE = "0000ff30-0000-1000-8000-00805f9b34fb";
    public static String GPIO_CHARACTERISTIC = "0000ff33-0000-1000-8000-00805f9b34fb";
  
    public static String TIO_SERVICE = "0000ff40-0000-1000-8000-00805f9b34fb";
    
    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
