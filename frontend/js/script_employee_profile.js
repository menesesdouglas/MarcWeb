
const urlBase = "http://localhost:8080/api/user";
const queryString = window.location.search;
const params = new URLSearchParams(queryString);

const userId = params.get("userId");
/// EVITANDO QUE O PÁGINA RECARRREGUE AO ENVIAR O FORMULÁRIO
document.getElementById("form-new-folga").addEventListener("submit", function (e) {
    e.preventDefault();
})

document.getElementById("closeModal-folga").addEventListener("click", () => {
    closeModalDetalhesFolga();
})

async function loadPage() {

    const auth = JSON.parse(localStorage.getItem("auth"));
    if (!auth) window.location.href = "/login.html";
    if (auth.role != "ADMIN") window.location.href = "/employee.html";


    if (userId == null || !userId.trim()) {
        pageNotFound();
    } else {
        const response = await fetch(`${urlBase}/${userId}?pageNumber=0`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${auth.token}`
            }
        });
        if (response.ok) {
            const dadosUsuario = await response.json();
            document.getElementById("nome").textContent = dadosUsuario.nome;
            document.getElementById("cargo").textContent = dadosUsuario.cargo;
            renderFolgas(dadosUsuario.folgas);
        } else {
            pageNotFound();
        }
    }

}

/// ===>>>>  


async function cadastrarFolga() {
    const auth = JSON.parse(localStorage.getItem("auth"));
    const dataInicio = document.getElementById("data-inicio").value;
    const dataFim = document.getElementById("data-fim").value;
    const justificativa = document.getElementById("justificativa").value;
    const tipo = document.getElementById("tipo").value;
    // VALIDAÇÃO QUE JÁ EXISTE NO FORMULÁRIO ( NEM É TÃO NECESSÁRIO)
    if (!dataInicio.trim() || !dataFim.trim() || justificativa.trim().length < 20) {
        alert("Preencha os campos obrigatórios");
        return;
    }
    const dadosFolga = {
        userId: userId,
        dataInicio: dataInicio,
        dataFim: dataFim,
        justificativa: justificativa,
        tipo: tipo
    }

    const response = await fetch(`http://localhost:8080/api/folgas`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${auth.token}`,
            "Content-Type": "application/json"
        },
        body: JSON.stringify(dadosFolga)
    })

    if (response.status == 201) {
        alert("Folga cadastrada com sucesso")
    } else {
        const erro = await response.json().erro;
        alert(erro);
    }
}


function renderFolgas(folgas) {
    const tabela = document.getElementById("table-folgas");
    if (!folgas.length) mensagemFolgasNotFound();
    else {
        folgas.forEach((folga, index) => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
            <td class="${folga.status.toLowerCase()}">${folga.status}</td>
            <td>${folga.tipo}</td>
            <td>${folga.dataInicio}</td>
            <td>${folga.dataFim}</td>
            <td>
            <label id="open-modal${folga.id}" for="modal-detalhes" class="btn">Detalhes</label>
            </td>
            `;
            tabela.append(tr);
            document.getElementById(`open-modal${folga.id}`).addEventListener("click", () => {
                openModal(folga, folgas);
            })
        })
    }
}

function mensagemFolgasNotFound() {
    document.getElementById("table-folgas").innerHTML = "<h3>Nenhuma folga encontrada!</h3>";
}

function openModal(folga, folgas) {
    document.getElementById("nome-user").textContent = folga.nomeFuncionario;
    document.getElementById("tipo-folga").textContent = folga.tipo;
    document.getElementById("inicio-folga").textContent = folga.dataInicio;
    document.getElementById("fim-folga").textContent = folga.dataFim;
    document.getElementById("justificativa-info").textContent = folga.justificativa;
    document.getElementById("status-folga").textContent = folga.status;
    document.getElementById("modal-folga").classList.add("active");

    const div = document.createElement("div");
    div.innerHTML = `
    <section style="display: flex; gap: 10px; justify-content: center;" id="action-folga-section">
                    <div class="btn-solucionar-folga ${folga.status == "Pendente" ? 'active' : ''}" id="btn-solucionar-folga">
                        <button onClick = "resolverFolga(${folga.id}, true)" id="btn-aprovar" class="btn-success btn">Aprovar</button>
                        <button onClick = "resolverFolga(${folga.id}, false)" id="btn-reprovar" class="btn-warning btn">Reprovar</button>
                    </div>
                    <button  id="btn-remover${folga.id}" class="btn-danger btn">Remover</button>
     </section>
    `;
    const container = document.getElementById("buttons");
    container.append(div);
    document.getElementById(`btn-remover${folga.id}`).addEventListener("click", () => {
        removerFolga(folga.id, folgas);
    });
}

function closeModalDetalhesFolga() {
    document.getElementById("modal-folga").classList.remove("active");
    document.getElementById("btn-solucionar-folga").classList.remove("active");
    document.getElementById("buttons").innerHTML = "";
}



/// RESOLVER FOLGA ♥♥♥♥♥♥♥♥♥
async function resolverFolga(idFolga, aprovada) {
    const confimado = confirm(`Confirma a ${aprovada ? "aprovação" : "desaprovação"} desta folga?`);
    if (!confimado) return;
    const auth = JSON.parse(localStorage.getItem("auth"));
    const response = await fetch(`http://localhost:8080/api/folgas/solucionar/${idFolga}/${aprovada}`, {
        headers: {
            "Authorization": `Bearer ${auth.token}`
        },
        method: 'PUT'
    })
    if (response.ok) {
        alert("Folga solucionada com sucesso");
        closeModalDetalhesFolga();
    } else {
        alert("Erro ao solucionar folga");
    }
}

/// REMOVER FOLGA ♥♥♥♥♥♥♥♥♥♥♥♥
async function removerFolga(idFolga, folgas) {
    const confimacao = confirm("Confirma a exclusão desta folga?");
    if (confimacao) {

        const auth = JSON.parse(localStorage.getItem("auth"));
        const response = await fetch(`http://localhost:8080/api/folgas/${idFolga}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${auth.token}`
            }
        });
        if (response.status == 204) {
            alert("Folga removida com sucesso");
            const folgasAtualizadas = folgas.filter(item => item.id != idFolga);
            renderFolgas(folgasAtualizadas);
            closeModalDetalhesFolga();
        }
        else {
            alert("Algo deu errado!")
        }
    }
}


function pageNotFound() {
    document.body.innerHTML = `
   <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 90vh; font-family: sans-serif; text-align: center; color: #333;">
   <h1 style="font-size: 8rem; margin: 0; color: #e74c3c;">404</h1>
   <h2 style="font-size: 1.5rem; margin: 10px 0;">Página Não Encontrada</h2>
   <p style="color: #666; max-width: 400px; line-height: 1.6;">
   O usuário que você está procurando não existe.
   </p>
   <a href="/" style="margin-top: 20px; padding: 10px 20px; background: #3498db; color: white; text-decoration: none; border-radius: 5px; font-weight: bold;">
    Voltar para o Início
  </a>
  </div>
  
   `;
}

