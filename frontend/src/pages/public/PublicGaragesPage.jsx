// M02 (Hiếu) – SCR-39: Danh sách gara liên kết công khai (UC-44)
import { Alert, Card, Col, Container, Row } from 'react-bootstrap';
import { garageApi } from '../../api/garageApi';
import { usePaged } from '../../hooks/usePaged';
import { Loading, Paging, SearchBox } from '../../components/ui';

export default function PublicGaragesPage() {
  const paged = usePaged(garageApi.listPublic, { q: '', province: '' }, 12);
  const { data, loading } = paged;

  return (
    <Container className="py-4">
      <h3 className="mb-1"><i className="bi bi-tools text-primary me-2" />Gara liên kết</h3>
      <p className="text-muted">Mạng lưới gara sửa chữa xe máy liên kết với InsuranceCard – nơi giám định và sửa chữa khi bạn yêu cầu bồi thường.</p>
      <Row className="g-2 mb-3">
        <Col md={7}><SearchBox value={paged.filters.q} onChange={(v) => paged.setFilter('q', v)} placeholder="Tên gara, địa chỉ hoặc số điện thoại..." /></Col>
        <Col md={5}><SearchBox value={paged.filters.province} onChange={(v) => paged.setFilter('province', v)} placeholder="Tỉnh / thành phố (VD: Hà Nội)" maxLength={50} /></Col>
      </Row>
      {paged.error && <Alert variant="danger">{paged.error}</Alert>}
      {loading && !data && <Loading />}
      {data?.content.length === 0 && <Alert variant="light" className="text-center">Không tìm thấy gara phù hợp.</Alert>}
      <Row className="g-3">
        {data?.content.map((g) => (
          <Col key={g.id} md={6} lg={4}>
            <Card className="h-100">
              <Card.Body>
                <div className="d-flex justify-content-between align-items-start mb-1">
                  <Card.Title as="h6" className="mb-0">{g.name}</Card.Title>
                  <span className="badge text-bg-light">{g.province}</span>
                </div>
                <div className="small text-muted mb-2">{g.garageCode}</div>
                <div className="small"><i className="bi bi-geo-alt me-1" />{g.address}</div>
                <div className="small"><i className="bi bi-telephone me-1" /><a href={`tel:${g.phone}`}>{g.phone}</a></div>
                {g.email && <div className="small"><i className="bi bi-envelope me-1" /><a href={`mailto:${g.email}`}>{g.email}</a></div>}
              </Card.Body>
            </Card>
          </Col>
        ))}
      </Row>
      <Paging data={data} onChange={paged.setPage} />
    </Container>
  );
}
