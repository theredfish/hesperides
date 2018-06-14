package org.hesperides.batch.token;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Profile({"batch","mongo"})
@Repository
public interface MongoTokenRepository extends MongoRepository<Token,String> {

    List<Token> findAllByStatus(int status);
    List<Token> findAllByType(String type);
    Token findByKey(String key);
//    List<Token>findAllByTypeAndByStatus(String type,int status);
    List<Token>findAllByTypeAndStatus(String type,int status);
    List<Token>findAllByTypeAndStatusNot(String type,int status);
}
