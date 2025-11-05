package vn.bxh.jobhunter.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResCompanyDTO {
    private long id;
    private String name;
    private String description;
    private String address;
    private String logo; 
    public String getLogoUrl() {
        if (logo == null || logo.isEmpty()) return null;
        try {
            return "/storage/company/" + java.net.URLEncoder.encode(logo, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "/storage/company/" + logo;
        }
    }
}

