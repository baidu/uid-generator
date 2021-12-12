package com.baidu.fsg.uid.worker;

/**
 * Strategy for assign workerId
 *
 * @Auther gongxiaoyue
 * @Date 2021-11-07
 */
public enum WorkerIdAssignerStrategy {

    DISPOSABLE(0, "disposable"),
    LOOP(1, "loop");

    private Integer value;
    private String name;

    public Integer getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    WorkerIdAssignerStrategy(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    /**
     * get WorkerIdAssignerStrategy by value. Default is DISPOSABLE
     * @param value
     * @return
     */
    public static WorkerIdAssignerStrategy valueOf(Integer value) {
        for (WorkerIdAssignerStrategy item : values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return DISPOSABLE;
    }
}
