export const extractTurkishPhoneDigits = (value: string) => {
  const digits = value.replace(/\D/g, '');

  if (digits.startsWith('90')) {
    return digits.slice(2, 12);
  }

  return digits.slice(0, 10);
};

export const isValidTurkishPhone = (value: string) => extractTurkishPhoneDigits(value).length === 10;

export const formatTurkishPhoneForRequest = (value: string) => `+90${extractTurkishPhoneDigits(value)}`;
