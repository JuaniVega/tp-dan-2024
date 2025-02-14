package isi.dan.ms.pedidos.mapper;

import org.springframework.stereotype.Component;

import isi.dan.ms.pedidos.dto.ObraDTO;
import isi.dan.ms.pedidos.modelo.Obra;

@Component
public class ObraMapper {

    public Obra transformarDTOEnObra(ObraDTO obraDTO){
        Obra obra = new Obra();
        obra.setId(obraDTO.getId());
        obra.setDireccion(obraDTO.getDireccion());

        return obra;
    }
    
}
