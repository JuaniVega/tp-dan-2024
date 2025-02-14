package isi.dan.ms.pedidos.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import isi.dan.ms.pedidos.modelo.PedidoCounter;

@Service
public class PedidoCounterService {
    @Autowired
    private MongoTemplate mongoTemplate;

    public int getNextPedidoNumber() {
        Query query = new Query(where("_id").is("pedido")); // Buscar por ID "pedido"
        Update update = new Update().inc("seq", 1); // Incrementar secuencia en 1
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);

        PedidoCounter counter = mongoTemplate.findAndModify(query, update, options, PedidoCounter.class);
        return (counter != null) ? counter.getSeq() : 1;
    }
}
