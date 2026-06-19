export default function Avatar({ photoUrl, initiales = '', size = 36 }) {
  if (photoUrl) {
    return (
      <img
        className="avatar-img"
        src={photoUrl}
        alt=""
        style={{ width: size, height: size }}
      />
    );
  }
  return (
    <span
      className="avatar-circle"
      style={{ width: size, height: size, fontSize: Math.round(size * 0.36) }}
    >
      {initiales}
    </span>
  );
}
