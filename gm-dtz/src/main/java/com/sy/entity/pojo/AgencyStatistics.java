package com.sy.entity.pojo;

import com.sy.mainland.util.db.annotation.Table;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by pc on 2017/4/18.
 */
@Table(alias = "agency_statistics")
public class AgencyStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer dateTime;
    private String agencyTotal;
    private String agencyCount;
    private String payCount;
    private String payTotal;

    public AgencyStatistics() {
    }

    public Integer getDateTime() {
        return dateTime;
    }

    public void setDateTime(Integer dateTime) {
        this.dateTime = dateTime;
    }

    public String getAgencyTotal() {
        return agencyTotal;
    }

    public void setAgencyTotal(String agencyTotal) {
        this.agencyTotal = agencyTotal;
    }

    public String getAgencyCount() {
        return agencyCount;
    }

    public void setAgencyCount(String agencyCount) {
        this.agencyCount = agencyCount;
    }

    public String getPayCount() {
        return payCount;
    }

    public void setPayCount(String payCount) {
        this.payCount = payCount;
    }

    public String getPayTotal() {
        return payTotal;
    }

    public void setPayTotal(String payTotal) {
        this.payTotal = payTotal;
    }
}
