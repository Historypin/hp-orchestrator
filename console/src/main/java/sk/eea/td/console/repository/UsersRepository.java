package sk.eea.td.console.repository;

import org.springframework.data.repository.CrudRepository;
import sk.eea.td.console.model.User;

public interface UsersRepository extends CrudRepository<User, String> {

    User findByUsername(String username);
}
