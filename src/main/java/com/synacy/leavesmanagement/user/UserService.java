package com.synacy.leavesmanagement.user;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser (UserRequest userRequest){
        // 1. Validate name
        if (userRequest.getName() == null || userRequest.getName().isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        if (userRepository.existsByName((userRequest.getName()))){
            throw new DuplicateUserNameException(userRequest.getName());
        }

        // 2. Validate role
        if (userRequest.getRole() == null) {
            throw new RoleNotFoundException("null");
        }

        // 3. Validate manager (if provided)
        if (userRequest.getManager() != null &&
                !userRepository.existsById(userRequest.getManager().getId())) {
            throw new ManagerNotFoundException(userRequest.getManager().getId());
        }

        // 4. Create user
        User user = new User(
                userRequest.getName(),
                userRequest.getRole(),
                userRequest.getManager(),
                userRequest.getLeaveCredits()
        );

        return userRepository.save(user);
    }

    /*
    *
    * Role dosent exists execption
    * Name is taken execptions like i dont know if this is a exception but it whousl be some warning no? since 2 people with same name
    * waht could we do with that
    *
    *manager dosent exits
    *
    * */
}
