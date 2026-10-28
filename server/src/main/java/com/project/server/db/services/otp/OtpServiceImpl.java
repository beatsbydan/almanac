package com.project.server.db.services.otp;

import com.project.server.db.models.Otp;
import com.project.server.db.repositories.OtpRepository;
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
}
