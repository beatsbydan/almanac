package com.project.server.db.services.otp;

import com.project.server.db.models.Otp;
import com.project.server.exceptions.NotFoundException;

public interface OtpService {
    Otp save(Otp otp);

    Otp findByOtp(String otp) throws NotFoundException;
}
