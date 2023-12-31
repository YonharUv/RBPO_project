package bks2101.kuraga.firstProject.service;

import bks2101.kuraga.firstProject.dto.GroupDto;
import bks2101.kuraga.firstProject.dto.StudentDto;
import bks2101.kuraga.firstProject.exceptions.GroupNotFoundByCurator;
import bks2101.kuraga.firstProject.exceptions.UserAlreadyExistsException;
import bks2101.kuraga.firstProject.exceptions.UserNotFoundByUsernameException;
import bks2101.kuraga.firstProject.entitys.Curator;
import bks2101.kuraga.firstProject.entitys.Group;
import bks2101.kuraga.firstProject.entitys.Meeting;
import bks2101.kuraga.firstProject.entitys.Student;
import bks2101.kuraga.firstProject.repository.CuratorRepository;
import bks2101.kuraga.firstProject.repository.GroupRepository;
import bks2101.kuraga.firstProject.repository.MeetingRepository;
import bks2101.kuraga.firstProject.repository.StudentRepository;
import bks2101.kuraga.firstProject.utils.MappingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static bks2101.kuraga.firstProject.utils.MappingUtil.mapToMeeting;
import static bks2101.kuraga.firstProject.utils.MappingUtil.mapToStudent;
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final CuratorRepository curatorRepository;
    private final MeetingRepository meetingRepository;
    private final StudentRepository studentRepository;
    public ResponseEntity<GroupDto> getGroupByName(String groupName) throws UserNotFoundByUsernameException {
        if (!groupRepository.existsByName(groupName)) {
            throw new UserNotFoundByUsernameException("Группа", groupName);
        }
        Group group = groupRepository.findByName(groupName);
        GroupDto groupDto = MappingUtil.mapToGroupDto(group);
        return ResponseEntity.ok(groupDto);
    }

    public ResponseEntity<GroupDto> createGroup(GroupDto groupDto) throws UserAlreadyExistsException {
        if (groupRepository.existsByName(groupDto.getName())) {
            throw new UserAlreadyExistsException("Группа", groupDto.getName());
        }
        Group group = new Group();
        groupRepository.save(group);
        if (groupDto.getCurator_email() != null && curatorRepository.existsByEmail(groupDto.getCurator_email())) {
            Curator curator = curatorRepository.getByEmail(groupDto.getCurator_email());
            group.setCurator(curator);
            if (groupDto.getMeetings() != null) {
                List<Meeting> meetings = groupDto.getMeetings().stream()
                        .map(meetingDto -> mapToMeeting(curator, meetingDto, group))
                        .collect(Collectors.toList());
                meetingRepository.saveAll(meetings);
                group.setMeetings(meetings);
            }
        }
        group.setName(groupDto.getName());
        group.setFaculty_name(groupDto.getFaculty_name());
        if (groupDto.getStudents() != null) {
            List<Student> students = groupDto.getStudents().stream()
                    .map(studentDto -> mapToStudent(studentDto, group))
                    .collect(Collectors.toList());
            group.setStudents(students);
            studentRepository.saveAll(students);
        }
        groupRepository.save(group);
        log.info("Group created successfully {}", groupDto.getName());
        return ResponseEntity.ok(MappingUtil.mapToGroupDto(group));
    }

    public ResponseEntity<Set<GroupDto>> getAllGroups() {
        List<Group> groups = groupRepository.findAll();
        Set<GroupDto> res = groups.stream()
                .map(MappingUtil::mapToGroupDto)
                .collect(Collectors.toSet());
        return  ResponseEntity.ok(res);
    }

    public ResponseEntity<GroupDto> updateGroup(String groupName, GroupDto groupDto) throws UserNotFoundByUsernameException, UserAlreadyExistsException {
        if (!groupRepository.existsByName(groupName)) {
            throw new UserNotFoundByUsernameException("Группа", groupName);
        }
        Group group = groupRepository.findByName(groupName);
        if (groupRepository.existsByName(groupDto.getName()) && !Objects.equals(groupDto.getName(), groupName)) {
            throw new UserAlreadyExistsException("Группа", groupName);
        }
        if (groupDto.getCurator_email() != null && curatorRepository.existsByEmail(groupDto.getCurator_email())) {
            Curator curator = curatorRepository.getByEmail(groupDto.getCurator_email());
            group.setCurator(curator);
            if (groupDto.getMeetings() != null) {
                List<Meeting> meetings = groupDto.getMeetings().stream()
                        .map(meetingDto -> mapToMeeting(curator, meetingDto, group))
                        .collect(Collectors.toList());
                group.setMeetings(meetings);
                meetingRepository.saveAll(meetings);
            }
        }
        group.setName(groupDto.getName());
        group.setFaculty_name(groupDto.getFaculty_name());
        if (groupDto.getStudents() != null) {
            List<Student> students = groupDto.getStudents().stream()
                    .map(studentDto -> mapToStudent(studentDto, group))
                    .collect(Collectors.toList());
            group.setStudents(students);
            studentRepository.saveAll(students);
        }
        groupRepository.save(group);
        log.info("Group updated successfully {}", groupName);
        return ResponseEntity.ok(MappingUtil.mapToGroupDto(group));
    }

    public ResponseEntity<String> deleteGroupByName(String groupName) throws UserNotFoundByUsernameException {
        if (!groupRepository.existsByName(groupName)) {
            throw new UserNotFoundByUsernameException("Группа", groupName);
        }
        Group group = groupRepository.findByName(groupName);
        group.setCurator(null);
        studentRepository.deleteAll(group.getStudents());
        meetingRepository.deleteAll(group.getMeetings());
        groupRepository.delete(group);
        log.info("Group deleted successfully {}", groupName);
        return ResponseEntity.ok("Группа " + groupName + " успешна удалена");
    }

    public ResponseEntity<List<StudentDto>> getGroupStudentsByCurator(String curatorEmail, String groupName) throws UserNotFoundByUsernameException, GroupNotFoundByCurator {
        if (!groupRepository.existsByName(groupName)) {
            throw new UserNotFoundByUsernameException("Группа", groupName);
        }
        Group group = groupRepository.findByName(groupName);
        if (!(curatorRepository.existsByEmail(curatorEmail)) || !Objects.equals(group.getCurator().getEmail(), curatorEmail)) {
            throw new GroupNotFoundByCurator(curatorEmail, groupName);
        }
        GroupDto groupDto = MappingUtil.mapToGroupDto(group);
        return ResponseEntity.ok(groupDto.getStudents());
    }
    public ResponseEntity<List<StudentDto>> getGroupStudents(String groupName) throws UserNotFoundByUsernameException, GroupNotFoundByCurator {
        if (!groupRepository.existsByName(groupName)) {
            throw new UserNotFoundByUsernameException("Группа", groupName);
        }
        Group group = groupRepository.findByName(groupName);
        GroupDto groupDto = MappingUtil.mapToGroupDto(group);
        return ResponseEntity.ok(groupDto.getStudents());
    }
}

