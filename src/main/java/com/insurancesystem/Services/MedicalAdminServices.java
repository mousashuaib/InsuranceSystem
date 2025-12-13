package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.HealthcareProviderClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicalAdminServices {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final HealthcareProviderClaimRepository claimRepository; // ✅ نضيفها هنا

    // ✅ تفعيل / تعطيل مستخدم
    public void toggleUserStatus(UUID id) {
        Client user = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getStatus() == MemberStatus.ACTIVE)
            user.setStatus(MemberStatus.DEACTIVATED);
        else
            user.setStatus(MemberStatus.ACTIVE);
        clientRepository.save(user);
    }

    private boolean hasRole(Client client, RoleName role) {
        return client.getRoles().stream().anyMatch(r -> r.getName() == role);
    }



    public Map<String, Object> getFullDashboardStats() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 🔹 كل المستخدمين
        List<Client> all = clientRepository.findAll();

        // 🔹 حساب الأعداد حسب الدور
        long doctors = all.stream().filter(c -> hasRole(c, RoleName.DOCTOR)).count();
        long labs = all.stream().filter(c -> hasRole(c, RoleName.LAB_TECH)).count();
        long radiologists = all.stream().filter(c -> hasRole(c, RoleName.RADIOLOGIST)).count();
        long pharmacists = all.stream().filter(c -> hasRole(c, RoleName.PHARMACIST)).count();
        long providersCount = doctors + labs + radiologists + pharmacists;

        // ✅ أكثر طبيب إرسالاً للمطالبات
        List<Object[]> topDoctors = claimRepository.findTopDoctorsByClaims();
        if (topDoctors.isEmpty()) {
            result.put("topDoctorName", "لا يوجد بيانات");
            result.put("claimCount", 0L);
        } else {
            Object[] row = topDoctors.get(0);
            result.put("topDoctorName", (String) row[0]);
            result.put("claimCount", ((Long) row[1]));
        }

        // 🧮 دمج كل الإحصاءات في كائن واحد
        result.put("doctors", doctors);
        result.put("labs", labs);
        result.put("radiologists", radiologists);
        result.put("pharmacists", pharmacists);
        result.put("total", providersCount);
        result.put("providersCount", providersCount);

        return result;
    }


}
