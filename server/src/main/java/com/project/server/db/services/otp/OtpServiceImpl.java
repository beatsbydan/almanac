package com.project.server.db.services.otp;

import com.project.server.db.models.Otp;
import com.project.server.db.repositories.OtpRepository;
import com.project.server.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService{
    private final OtpRepository repository;

    @Override
    public Otp save(Otp otp) {
        return repository.save(otp);
    }

    @Override
    public Otp findByOtp(String otp) throws NotFoundException {
        return repository.findByOtp(otp)
                .orElseThrow(() -> new NotFoundException(String.format("OTP: %s, was not found", otp)));
    }
}
