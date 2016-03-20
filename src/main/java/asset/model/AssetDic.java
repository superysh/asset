package asset.model;

// Generated 2016-3-2 22:49:03 by Hibernate Tools 3.4.0.CR1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * AssetDic generated by hbm2java
 */
@Entity
@Table(name = "asset_dic", catalog = "asset")
public class AssetDic implements java.io.Serializable {

	private Integer dicId;
	private String dicKey;
	private String dicValue;
	private Integer dicType;

	public AssetDic() {
	}

	public AssetDic(String dicKey, String dicValue, Integer dicType) {
		this.dicKey = dicKey;
		this.dicValue = dicValue;
		this.dicType = dicType;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "dic_id", unique = true, nullable = false)
	public Integer getDicId() {
		return this.dicId;
	}

	public void setDicId(Integer dicId) {
		this.dicId = dicId;
	}

	@Column(name = "dic_key", nullable = false)
	public String getDicKey() {
		return this.dicKey;
	}

	public void setDicKey(String dicKey) {
		this.dicKey = dicKey;
	}

	@Column(name = "dic_value", nullable = false)
	public String getDicValue() {
		return this.dicValue;
	}

	public void setDicValue(String dicValue) {
		this.dicValue = dicValue;
	}

	@Column(name = "dic_type", nullable = false)
	public Integer getDicType() {
		return this.dicType;
	}

	public void setDicType(Integer dicType) {
		this.dicType = dicType;
	}

}
