import { Vehicle } from '../models/vehicle';

const img = (id: string) =>
  `https://images.unsplash.com/${id}?auto=format&fit=crop&w=1200&q=80`;

export const VEHICLES: Vehicle[] = [
  {
    id: 'bugatti-chiron',
    name: 'Chiron Super Sport',
    brand: 'Bugatti',
    year: 2024,
    engine: 'W16 8.0L Quad-Turbo',
    power: 1600,
    torque: '1600 Nm',
    topSpeed: 440,
    acceleration: '2.3s',
    price: 12500000,
    image: img('photo-1618843479313-40f8afb4b4d8'),
    gallery: [img('photo-1618843479313-40f8afb4b4d8'), img('photo-1503376780353-7e6692767b70')],
    description:
      'O ápice da engenharia francesa. O Chiron Super Sport combina artesanato exclusivo com performance absoluta, entregando uma experiência de condução reservada a poucos no mundo.'
  },
  {
    id: 'bugatti-mistral',
    name: 'W16 Mistral',
    brand: 'Bugatti',
    year: 2023,
    engine: 'W16 8.0L Quad-Turbo',
    power: 1578,
    torque: '1600 Nm',
    topSpeed: 420,
    acceleration: '2.4s',
    price: 14000000,
    image: img('photo-1583121274602-3ceb120395a5'),
    gallery: [img('photo-1583121274602-3ceb120395a5')],
    description:
      'Roadster definitivo da Bugatti. Design aerodinâmico esculpido pelo vento com o icônico motor W16 exposto em pura celebração mecânica.'
  },
  {
    id: 'mclaren-p1',
    name: 'P1',
    brand: 'McLaren',
    year: 2015,
    engine: 'V8 3.8L Bi-Turbo Híbrido',
    power: 916,
    torque: '900 Nm',
    topSpeed: 350,
    acceleration: '2.8s',
    price: 8500000,
    image: img('photo-1621138807254-6e1cb1b5e0e0'),
    gallery: [img('photo-1621138807254-6e1cb1b5e0e0')],
    description:
      'Hypercar híbrido que redefiniu limites. Carbono MonoCage, aerodinâmica ativa e herança de pista em cada curva.'
  },
  {
    id: 'mclaren-765lt',
    name: '765LT',
    brand: 'McLaren',
    year: 2024,
    engine: 'V8 4.0L Twin-Turbo',
    power: 765,
    torque: '800 Nm',
    topSpeed: 330,
    acceleration: '2.7s',
    price: 3200000,
    image: img('photo-1544636331-e26879cd4d6b'),
    gallery: [img('photo-1544636331-e26879cd4d6b')],
    description:
      'Longtail moderno com peso reduzido e downforce ampliado. Precisão cirúrgica inspirada nas vitórias em Le Mans.'
  },
  {
    id: 'ferrari-sf90',
    name: 'SF90 Stradale',
    brand: 'Ferrari',
    year: 2024,
    engine: 'V8 4.0L Bi-Turbo + Elétrico',
    power: 1000,
    torque: '800 Nm',
    topSpeed: 340,
    acceleration: '2.5s',
    price: 4500000,
    image: img('photo-1580274454331-22ca8d787f03'),
    gallery: [img('photo-1580274454331-22ca8d787f03')],
    description:
      'Primeiro Ferrari plug-in hybrid. Três motores elétricos e V8 twin-turbo em harmonia para performance sem compromissos.'
  },
  {
    id: 'ferrari-daytona',
    name: 'Daytona SP3',
    brand: 'Ferrari',
    year: 2023,
    engine: 'V12 6.5L Atmosférico',
    power: 840,
    torque: '697 Nm',
    topSpeed: 340,
    acceleration: '2.85s',
    price: 12000000,
    image: img('photo-1563720223185-11003d516935'),
    gallery: [img('photo-1563720223185-11003d516935')],
    description:
      'Ícone Icona limitado. Design nostálgico dos anos 60 com V12 atmosférico — pura emoção italiana.'
  },
  {
    id: 'lambo-revuelto',
    name: 'Revuelto',
    brand: 'Lamborghini',
    year: 2024,
    engine: 'V12 6.5L Híbrido',
    power: 1015,
    torque: '725 Nm',
    topSpeed: 350,
    acceleration: '2.5s',
    price: 5200000,
    image: img('photo-1544636331-e26879cd4d6b'),
    gallery: [img('photo-1544636331-e26879cd4d6b')],
    description:
      'Sucessor espiritual do Aventador. Linhas agressivas, V12 híbrido e três motores elétricos para torque instantâneo.'
  },
  {
    id: 'lambo-huracan-sto',
    name: 'Huracán STO',
    brand: 'Lamborghini',
    year: 2023,
    engine: 'V10 5.2L Atmosférico',
    power: 640,
    torque: '565 Nm',
    topSpeed: 310,
    acceleration: '3.0s',
    price: 2800000,
    image: img('photo-1542362567-b07e543c53a2'),
    gallery: [img('photo-1542362567-b07e543c53a2')],
    description:
      'Homologado para rua com DNA de Super Trofeo. Aerodinâmica otimizada e peso mínimo para máxima conexão com a pista.'
  },
  {
    id: 'koenig-jesko',
    name: 'Jesko Absolut',
    brand: 'Koenigsegg',
    year: 2024,
    engine: 'V8 5.0L Twin-Turbo',
    power: 1600,
    torque: '1500 Nm',
    topSpeed: 499,
    acceleration: '2.5s',
    price: 11000000,
    image: img('photo-1494976388531-d1058494cdd8'),
    gallery: [img('photo-1494976388531-d1058494cdd8')],
    description:
      'Projetado para quebrar recordes de velocidade. Transmissão Light Speed e engenharia sueca de precisão extrema.'
  },
  {
    id: 'koenig-gemera',
    name: 'Gemera',
    brand: 'Koenigsegg',
    year: 2023,
    engine: 'V8 3.0L Freevalve + Elétrico',
    power: 1700,
    torque: '3500 Nm',
    topSpeed: 400,
    acceleration: '1.9s',
    price: 9500000,
    image: img('photo-1503376780353-7e6692767b70'),
    gallery: [img('photo-1503376780353-7e6692767b70')],
    description:
      'Mega-GT de quatro lugares. O único carro de produção com motor Freevalve e torque colossal em silêncio elétrico.'
  },
  {
    id: 'porsche-911-gt3',
    name: '911 GT3 RS',
    brand: 'Porsche',
    year: 2024,
    engine: 'Flat-6 4.0L Atmosférico',
    power: 525,
    torque: '465 Nm',
    topSpeed: 296,
    acceleration: '3.2s',
    price: 2100000,
    image: img('photo-1503376780353-7e6692767b70'),
    gallery: [img('photo-1503376780353-7e6692767b70')],
    description:
      'Referência em precisão alemã. DRS, asa ativa e motor aspirado que gira até 9000 rpm — puro prazer analógico.'
  },
  {
    id: 'porsche-taycan',
    name: 'Taycan Turbo S',
    brand: 'Porsche',
    year: 2024,
    engine: 'Dual Motor Elétrico',
    power: 761,
    torque: '1050 Nm',
    topSpeed: 260,
    acceleration: '2.8s',
    price: 980000,
    image: img('photo-1614162692292-7a56aaaabe84'),
    gallery: [img('photo-1614162692292-7a56aaaabe84')],
    description:
      'Elétrico com alma Porsche. Chassi baixo, recarga 800V e dinâmica que desafia a gravidade em silêncio absoluto.'
  },
  {
    id: 'bmw-m8',
    name: 'M8 Competition',
    brand: 'BMW',
    year: 2024,
    engine: 'V8 4.4L Twin-Turbo',
    power: 625,
    torque: '750 Nm',
    topSpeed: 305,
    acceleration: '3.2s',
    price: 890000,
    image: img('photo-1555215695-3004980ad54e'),
    gallery: [img('photo-1555215695-3004980ad54e')],
    description:
      'Grand tourer M com luxo e potência. Interior artesanal, xDrive e acabamento em carbono opcional.'
  },
  {
    id: 'bmw-m4-csl',
    name: 'M4 CSL',
    brand: 'BMW',
    year: 2023,
    engine: 'Inline-6 3.0L Twin-Turbo',
    power: 550,
    torque: '650 Nm',
    topSpeed: 302,
    acceleration: '3.5s',
    price: 1200000,
    image: img('photo-1617814076367-7594a49d0e33'),
    gallery: [img('photo-1617814076367-7594a49d0e33')],
    description:
      'Edição limitada CSL. Peso reduzido, suspensão ajustada para pista e herança dos M3 CSL clássicos.'
  },
  {
    id: 'amg-gt-black',
    name: 'AMG GT Black Series',
    brand: 'Mercedes-AMG',
    year: 2023,
    engine: 'V8 4.0L Twin-Turbo',
    power: 730,
    torque: '800 Nm',
    topSpeed: 325,
    acceleration: '3.2s',
    price: 3500000,
    image: img('photo-1618843479313-40f8afb4b4d8'),
    gallery: [img('photo-1618843479313-40f8afb4b4d8')],
    description:
      'O AMG mais extremo já produzido. Aerodinâmica de F1, V8 biturbo e downforce que cola o carro ao asfalto.'
  },
  {
    id: 'amg-one',
    name: 'AMG ONE',
    brand: 'Mercedes-AMG',
    year: 2024,
    engine: 'V6 1.6L Turbo Híbrido F1',
    power: 1063,
    torque: '—',
    topSpeed: 352,
    acceleration: '2.9s',
    price: 15000000,
    image: img('photo-1617814076367-7594a49d0e33'),
    gallery: [img('photo-1617814076367-7594a49d0e33')],
    description:
      'Tecnologia de Fórmula 1 homologada para rua. Híbrido derivado do power unit Mercedes-AMG Petronas.'
  },
  {
    id: 'audi-r8',
    name: 'R8 V10 Performance',
    brand: 'Audi',
    year: 2024,
    engine: 'V10 5.2L FSI',
    power: 620,
    torque: '570 Nm',
    topSpeed: 331,
    acceleration: '3.1s',
    price: 1450000,
    image: img('photo-1606664515524-ed2f786a0bd6'),
    gallery: [img('photo-1606664515524-ed2f786a0bd6')],
    description:
      'Última geração do V10 aspirado da Audi. Quattro permanente e design configurador premium em cada detalhe.'
  },
  {
    id: 'audi-e-tron-gt',
    name: 'e-tron GT RS',
    brand: 'Audi',
    year: 2024,
    engine: 'Dual Motor Elétrico',
    power: 646,
    torque: '830 Nm',
    topSpeed: 250,
    acceleration: '3.3s',
    price: 780000,
    image: img('photo-1614162692292-7a56aaaabe84'),
    gallery: [img('photo-1614162692292-7a56aaaabe84')],
    description:
      'Gran Turismo elétrico com silhueta esculpida. Interior minimalista inspirado no configurador Audi digital.'
  },
  {
    id: 'rolls-phantom',
    name: 'Phantom Extended',
    brand: 'Rolls-Royce',
    year: 2024,
    engine: 'V12 6.75L Twin-Turbo',
    power: 571,
    torque: '900 Nm',
    topSpeed: 250,
    acceleration: '5.3s',
    price: 6800000,
    image: img('photo-1563720223185-11003d516935'),
    gallery: [img('photo-1563720223185-11003d516935')],
    description:
      'O pináculo do luxo automotivo. Starlight Headliner, artesanato bespoke e presença que transcende o tempo.'
  },
  {
    id: 'rolls-cullinan',
    name: 'Cullinan Black Badge',
    brand: 'Rolls-Royce',
    year: 2023,
    engine: 'V12 6.75L Twin-Turbo',
    power: 600,
    torque: '900 Nm',
    topSpeed: 250,
    acceleration: '5.0s',
    price: 5200000,
    image: img('photo-1631296337651-6e9a0e2b0a0e'),
    gallery: [img('photo-1631296337651-6e9a0e2b0a0e')],
    description:
      'SUV ultra-luxo com espírito Black Badge. Capacidade off-road discreta sem sacrificar o conforto Rolls-Royce.'
  },
  {
    id: 'aston-valkyrie',
    name: 'Valkyrie',
    brand: 'Aston Martin',
    year: 2024,
    engine: 'V12 6.5L + Híbrido Cosworth',
    power: 1160,
    torque: '900 Nm',
    topSpeed: 350,
    acceleration: '2.5s',
    price: 18000000,
    image: img('photo-1542362567-b07e543c53a2'),
    gallery: [img('photo-1542362567-b07e543c53a2')],
    description:
      'Co-criado com Red Bull Racing. Chassi estrutural e aerodinâmica ground-effect para performance de protótipo.'
  },
  {
    id: 'aston-dbs',
    name: 'DBS Superleggera',
    brand: 'Aston Martin',
    year: 2023,
    engine: 'V12 5.2L Twin-Turbo',
    power: 725,
    torque: '900 Nm',
    topSpeed: 340,
    acceleration: '3.4s',
    price: 2900000,
    image: img('photo-1606664515524-ed2f786a0bd6'),
    gallery: [img('photo-1606664515524-ed2f786a0bd6')],
    description:
      'GT britânico com alma de James Bond. Superleggera em carbono e V12 que define a elegância em alta velocidade.'
  },
  {
    id: 'pagani-huayra',
    name: 'Huayra BC',
    brand: 'Pagani',
    year: 2023,
    engine: 'V12 6.0L Twin-Turbo AMG',
    power: 800,
    torque: '1100 Nm',
    topSpeed: 383,
    acceleration: '2.8s',
    price: 14000000,
    image: img('photo-1492144534655-ae79c964c9d7'),
    gallery: [img('photo-1492144534655-ae79c964c9d7')],
    description:
      'Obra de arte sobre rodas. Carroceria em titânio e carbono, motor AMG e detalhes que rivalizam joalheria suíça.'
  },
  {
    id: 'pagani-utopia',
    name: 'Utopia',
    brand: 'Pagani',
    year: 2024,
    engine: 'V12 6.0L Twin-Turbo AMG',
    power: 864,
    torque: '1100 Nm',
    topSpeed: 350,
    acceleration: '2.8s',
    price: 16000000,
    image: img('photo-1494976388531-d1058494cdd8'),
    gallery: [img('photo-1494976388531-d1058494cdd8')],
    description:
      'A nova era Pagani. Design atemporal, V12 biturbo e artesanato italiano em cada parafuso visível.'
  }
];
