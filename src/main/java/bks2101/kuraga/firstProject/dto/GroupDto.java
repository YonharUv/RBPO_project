package bks2101.kuraga.firstProject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupDto {
    private String name;
    private String faculty_name;
    private String curator_email;
    private List<StudentDto> students;
    private List<MeetingDto> meetings;
}
