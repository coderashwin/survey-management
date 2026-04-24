package com.sbi.branchdarpan.service;

import static com.sbi.branchdarpan.model.dto.user.UserDtos.HrmsUserResponse;

import org.springframework.stereotype.Service;

@Service
public class HrmsService {

    public HrmsUserResponse fetch(String pfid) {
        String suffix = pfid.length() >= 4 ? pfid.substring(pfid.length() - 4) : pfid;
        return new HrmsUserResponse(
            pfid,
            "Employee " + pfid,
            "employee" + suffix + "@sbi.co.in",
            "900000" + String.format("%04d", Integer.parseInt(suffix.replaceAll("\\D", "0"))),
            "Manager",
            "C01",
            "Delhi Circle",
            "A01",
            "Administrative Office 1",
            "R01",
            "Regional Business Office 1",
            "B" + String.format("%03d", Integer.parseInt(suffix.replaceAll("\\D", "0")) % 1000),
            "Branch " + suffix
        );
    }
}
